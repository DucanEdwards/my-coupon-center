package com.wsz.coupon.customer.service;

import com.google.common.collect.Lists;
import com.wsz.coupon.calculation.api.beans.ShoppingCart;
import com.wsz.coupon.calculation.api.beans.SimulationOrder;
import com.wsz.coupon.calculation.api.beans.SimulationResponse;
import com.wsz.coupon.customer.api.beans.RequestCoupon;
import com.wsz.coupon.customer.api.beans.SearchCoupon;
import com.wsz.coupon.customer.api.enums.CouponStatus;
import com.wsz.coupon.customer.dao.CouponDao;
import com.wsz.coupon.customer.dao.entity.Coupon;
import com.wsz.coupon.customer.feign.CalculationService;
import com.wsz.coupon.customer.feign.TemplateService;
import com.wsz.coupon.customer.service.intf.CouponCustomerService;
import com.wsz.coupon.template.api.beans.CouponInfo;
import com.wsz.coupon.template.api.beans.CouponTemplateInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wsz.coupon.customer.constant.Constant.TRAFFIC_VERSION;

@Service
@Slf4j
public class CouponCustomerServiceImpl implements CouponCustomerService {
    @Autowired
    private CouponDao couponDao;

    @Resource
    private TemplateService templateService;

    @Resource
    private CalculationService calculationService;

    /**
     * 用户领取优惠劵
     * @param request
     * @return
     */
    @Override
    public Coupon requestCoupon(RequestCoupon request) {
        // CouponTemplateInfo templateInfo = templateService.loadTemplateInfo(request.getCouponTemplateId());
        // CouponTemplateInfo templateInfo = webClientBuilder.build()
        //         .get()
        //         .uri("http://coupon-template-serv/template/getTemplate?id=" + request.getCouponTemplateId())
        //         // 将流量标记传入WebClient请求的Header中
        //         .header(TRAFFIC_VERSION, request.getTrafficVersion())
        //         .retrieve()
        //         .bodyToMono(CouponTemplateInfo.class)
        //         .block();
        CouponTemplateInfo templateInfo = templateService.getTemplate(request.getCouponTemplateId());

        // 模版不存在报错
        if (templateInfo==null) {
            log.error("invalid template id={}", request.getCouponTemplateId());
            throw new IllegalArgumentException("Invalid template id");
        }

        // 模版不能过期
        long now = Calendar.getInstance().getTimeInMillis();
        Long deadline = templateInfo.getRule().getDeadline();
        if (deadline!=null && now>=deadline || BooleanUtils.isFalse(templateInfo.getAvailable())){
            log.error("template is not available id={}", request.getCouponTemplateId());
            throw new IllegalArgumentException("template is unavailable");
        }

        // 用户领劵数超过上线
        long count = couponDao.countByUserIdAndTemplateId(request.getUserId(), request.getCouponTemplateId());
        if (count>=templateInfo.getRule().getLimitation()){
            log.error("exceeds maximum number");
            throw new IllegalArgumentException("exceeds maximum number");
        }

        Coupon coupon = Coupon.builder()
                .templateId(request.getCouponTemplateId())
                .userId(request.getUserId())
                .shopId(templateInfo.getShopId())
                .status(CouponStatus.AVAILABLE)
                .build();
        couponDao.save(coupon);
        return coupon;
    }

    @Override
    @Transactional
    public ShoppingCart placeOrder(ShoppingCart order) {
        if (CollectionUtils.isEmpty(order.getProducts())) {
            log.error("invalid check out request, order={}", order);
            throw new IllegalArgumentException("cart is empty");
        }

        Coupon coupon=null;
        if (order.getCouponId()!=null) {
            // 有优惠劵，验证是否可用，并且是当前客户的
            Coupon example=Coupon.builder()
                    .userId(order.getUserId())
                    .status(CouponStatus.AVAILABLE)
                    .id(order.getCouponId())
                    .build();
            coupon = couponDao.findAll(Example.of(example))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));

            CouponInfo couponInfo = CouponConverter.convertToCoupon(coupon);
            couponInfo.setTemplate(templateService.getTemplate(coupon.getTemplateId()));

            order.setCouponInfos(Lists.newArrayList(couponInfo));
        }

        // order清算
        // 单体方式
        // ShoppingCart checkoutInfo = calculationService.calculateOrderPrice(order);
        // WebClient调用方式
        // ShoppingCart checkoutInfo = webClientBuilder.build().post()
        //         .uri("http://coupon-calculation-serv/calculator/checkout")
        //         .bodyValue(order)
        //         .retrieve()
        //         .bodyToMono(ShoppingCart.class)
        //         .block();
        ShoppingCart checkoutInfo = calculationService.checkout(order);

        if (coupon!=null){
            // 如果优惠券没有被结算掉，而用户传递了优惠券，报错提示该订单满足不了优惠条件
            if (CollectionUtils.isEmpty(checkoutInfo.getCouponInfos())) {
                log.error("cannot apply coupon to order, couponId={}", coupon.getId());
                throw new IllegalArgumentException("coupon is not applicable to this order");
            }
            log.info("update coupon status to used, couponId={}", coupon.getId());
            coupon.setStatus(CouponStatus.USED);
            couponDao.save(coupon);
        }
        return checkoutInfo;
    }

    @Override
    public SimulationResponse simulateOrderPrice(SimulationOrder order) {
        List<CouponInfo> couponInfos = Lists.newArrayList();
        // 挨个循环，把优惠券信息加载出来
        // 高并发场景下不能这么一个个循环，更好的做法是批量查询
        // 而且券模板一旦创建不会改内容，所以在创建端做数据异构放到缓存里，使用端从缓存捞template信息
        for (Long couponId : order.getCouponIDs()) {
            Coupon example = Coupon.builder()
                    .userId(order.getUserId())
                    .id(couponId)
                    .status(CouponStatus.AVAILABLE)
                    .build();
            Optional<Coupon> couponOptional = couponDao.findAll(Example.of(example))
                    .stream()
                    .findFirst();
            // 加载优惠券模板信息
            if (couponOptional.isPresent()) {
                Coupon coupon = couponOptional.get();
                CouponInfo couponInfo = CouponConverter.convertToCoupon(coupon);
                couponInfo.setTemplate(templateService.getTemplate(coupon.getTemplateId()));
                couponInfos.add(couponInfo);
            }
        }
        order.setCouponInfos(couponInfos);

        // 调用接口试算服务
        // return calculationService.simulateOrder(order);
        // return webClientBuilder.build().post()
        //         .uri("http://coupon-calculation-serv/calculator/simulate")
        //         .bodyValue(order)
        //         .retrieve()
        //         .bodyToMono(SimulationResponse.class)
        //         .block();
        return calculationService.simulate(order);
    }


    /**
     * 逻辑删除优惠劵
     * @param userId
     * @param couponId
     */
    @Override
    public void deleteCoupon(Long userId, Long couponId) {
        Coupon example=Coupon.builder()
                .userId(userId)
                .id(couponId)
                .status(CouponStatus.AVAILABLE)
                .build();
        Coupon coupon = couponDao.findAll(Example.of(example))
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find available coupon"));
        coupon.setStatus(CouponStatus.INACTIVE);
        couponDao.save(coupon);
    }

    /**
     * 用户查询优惠劵
     * @param request
     * @return
     */
    @Override
    public List<CouponInfo> findCoupon(SearchCoupon request) {
        // 在真正的生产环境，这个接口需要做分页查询，并且查询条件要封装成一个类
        Coupon example = Coupon.builder()
                .userId(request.getUserId())
                .status(CouponStatus.convert(request.getCouponStatus()))
                .shopId(request.getShopId())
                .build();

        List<Coupon> coupons = couponDao.findAll(Example.of(example));
        if (coupons.isEmpty()) {
            return Lists.newArrayList();
        }

        List<Long> templateIds = coupons.stream()
                .map(Coupon::getTemplateId)
                .collect(Collectors.toList());
        // Map<Long, CouponTemplateInfo> templateMap = templateService.getTemplateInfoMap(templateIds);
        // Map<Long, CouponTemplateInfo> templateMap = webClientBuilder.build().get()
        //         .uri("http://coupon-template-serv/template/getBatch?ids=" + templateIds)
        //         .retrieve()
        //         .bodyToMono(new ParameterizedTypeReference<Map<Long, CouponTemplateInfo>>() {
        //         })
        //         .block();
        Map<Long, CouponTemplateInfo> templateMap = templateService.getTemplateInBatch(templateIds);

        coupons.stream().forEach(c->c.setTemplateInfo(templateMap.get(c.getTemplateId())));

        return coupons.stream()
                .map(CouponConverter::convertToCoupon)
                .collect(Collectors.toList());
    }
}
