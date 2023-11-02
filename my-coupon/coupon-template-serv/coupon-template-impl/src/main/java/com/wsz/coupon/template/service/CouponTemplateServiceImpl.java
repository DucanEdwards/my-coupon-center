package com.wsz.coupon.template.service;

import com.wsz.coupon.template.api.beans.CouponTemplateInfo;
import com.wsz.coupon.template.api.beans.PagedCouponTemplateInfo;
import com.wsz.coupon.template.api.beans.TemplateSearchParams;
import com.wsz.coupon.template.api.enums.CouponType;
import com.wsz.coupon.template.converter.CouponTemplateConverter;
import com.wsz.coupon.template.dao.CouponTemplateDao;
import com.wsz.coupon.template.dao.entity.CouponTemplate;
import com.wsz.coupon.template.service.intf.CouponTemplateService;
import com.wsz.coupon.template.service.intf.CouponTemplateServiceTCC;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponTemplateServiceImpl implements CouponTemplateServiceTCC {
    @Autowired
    private CouponTemplateDao couponTemplateDao;

    /**
     * 创建优惠劵模版
     * @param request
     * @return
     */
    @Override
    public CouponTemplateInfo createTemplate(CouponTemplateInfo request) {
        // 单个门店最多可以创建100张优惠券模板
        if (request.getShopId()!=null){
            Integer count = couponTemplateDao.countByShopIdAndAvailable(request.getShopId(), true);
            if (count>=100){
                log.error("the totals of coupon template exceeds maximum number");
                throw new UnsupportedOperationException("exceeded the maximum of coupon templates that you can create");
            }
        }
        CouponTemplate template = CouponTemplate.builder()
                .name(request.getName())
                .description(request.getDesc())
                .category(CouponType.convert(request.getType()))
                .available(true)
                .shopId(request.getShopId())
                .rule(request.getRule())
                .build();
        template = couponTemplateDao.save(template);
        return CouponTemplateConverter.converterTemplateinfo(template);
    }

    /**
     * 克隆优惠劵
     * @param templateId
     * @return
     */
    @Override
    public CouponTemplateInfo cloneTemplate(Long templateId) {
        log.info("cloning template id {}", templateId);
        CouponTemplate source = couponTemplateDao.findById(templateId).
                orElseThrow(()-> new IllegalArgumentException("invalid template ID"));

        CouponTemplate target = new CouponTemplate();
        BeanUtils.copyProperties(source,target);

        target.setAvailable(true);
        target.setId(null);
        couponTemplateDao.save(target);
        return CouponTemplateConverter.converterTemplateinfo(target);
    }

    /**
     * 分页搜索优惠劵
     * @param request
     * @return
     */
    @Override
    public PagedCouponTemplateInfo search(TemplateSearchParams request) {
        // 构建查询的Example对象
        CouponTemplate example = CouponTemplate.builder()
                .name(request.getName())
                .category(CouponType.convert(request.getType()))
                .shopId(request.getShopId())
                .available(request.getAvailable())
                .build();
        // 获取分页数据
        PageRequest page = PageRequest.of(request.getPage(), request.getPageSize());
        Page<CouponTemplate> result = couponTemplateDao.findAll(Example.of(example), page);
        List<CouponTemplateInfo> couponTemplateInfos = result.stream()
                .map(CouponTemplateConverter::converterTemplateinfo)
                .collect(Collectors.toList());

        PagedCouponTemplateInfo response = PagedCouponTemplateInfo.builder()
                .templates(couponTemplateInfos)
                .page(request.getPage())
                .total(result.getTotalElements())
                .build();
        return response;
    }

    /**
     * 通过模版id查询优惠劵模版
     * @param id
     * @return
     */
    @Override
    public CouponTemplateInfo loadTemplateInfo(Long id) {
        Optional<CouponTemplate> couponTemplate = couponTemplateDao.findById(id);
        return couponTemplate.isPresent() ? CouponTemplateConverter.converterTemplateinfo(couponTemplate.get()) : null;
    }

    /**
     * 让优惠劵无效
     * @param id
     */
    @Override
    public void deleteTemplate(Long id) {
        int rows = couponTemplateDao.makeCouponUnavailable(id);
        if (rows == 0) {
            throw new IllegalArgumentException("Template Not Found: " + id);
        }
    }

    /**
     * 批量读取模版
     */
    @Override
    public Map<Long, CouponTemplateInfo> getTemplateInfoMap(Collection<Long> ids) {
        List<CouponTemplate> templates = couponTemplateDao.findAllById(ids);
        return templates.stream()
                .map(CouponTemplateConverter::converterTemplateinfo)
                .collect(Collectors.toMap(CouponTemplateInfo::getId, Function.identity()));
    }

    @Override
    @Transactional
    public void deleteTemplateTCC(Long id) {
        CouponTemplate example = CouponTemplate.builder()
                .available(true)
                .locked(false)
                .id(id)
                .build();
        CouponTemplate template = couponTemplateDao.findAll(Example.of(example))
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Template Not Found"));
        template.setLocked(true);
        couponTemplateDao.save(template);
    }

    @Override
    @Transactional
    // Commit阶段，执行主体业务逻辑，即删除优惠劵
    public void deleteTemplateCommit(BusinessActionContext context) {
        Long id = Long.parseLong(context.getActionContext("id").toString());
        CouponTemplate template = couponTemplateDao.findById(id).get();

        template.setAvailable(false);
        template.setLocked(false);
        couponTemplateDao.save(template);

        log.info("TCC committed");
    }

    @Override
    @Transactional
    // 对应TCC的cacal阶段，如果try或Commit阶段发生异常，就会触发全局回滚，将Rollback指令发送给每个分支事务
    public void deleteTemplateCancel(BusinessActionContext context) {
        long id = Long.parseLong(context.getActionContext("id").toString());
        Optional<CouponTemplate> templateOptional = couponTemplateDao.findById(id);
        if (templateOptional.isPresent()) {
            CouponTemplate template = templateOptional.get();
            template.setLocked(false);
            template.setAvailable(true);
            couponTemplateDao.save(template);
        }
        log.info("TCC cancel");
    }
}
