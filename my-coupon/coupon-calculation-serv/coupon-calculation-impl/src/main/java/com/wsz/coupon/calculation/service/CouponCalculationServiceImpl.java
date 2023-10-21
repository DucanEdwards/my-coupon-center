package com.wsz.coupon.calculation.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wsz.coupon.calculation.api.beans.ShoppingCart;
import com.wsz.coupon.calculation.api.beans.SimulationOrder;
import com.wsz.coupon.calculation.api.beans.SimulationResponse;
import com.wsz.coupon.calculation.service.intf.CouponCalculationService;
import com.wsz.coupon.calculation.template.CouponTemplateFactory;
import com.wsz.coupon.calculation.template.RuleTemplate;
import com.wsz.coupon.template.api.beans.CouponInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CouponCalculationServiceImpl implements CouponCalculationService {
    @Autowired
    private CouponTemplateFactory couponTemplateFactory;

    // 优惠劵结算
    // 通过factory决定底层rule
    @Override
    public ShoppingCart calculateOrderPrice(ShoppingCart cart) {
        log.info("calculate order price: {}", JSON.toJSONString(cart));
        RuleTemplate ruleTemplate = couponTemplateFactory.getTemplate(cart);
        return ruleTemplate.calculate(cart);
    }


    // 对所有优惠券进行试算
    @Override
    public SimulationResponse simulateOrder(SimulationOrder simulationOrder) {
        SimulationResponse simulationResponse = new SimulationResponse();
        long minOrderPrice = Long.MAX_VALUE;

        // 计算每一个优惠劵的订单价格
        for (CouponInfo couponInfo:simulationOrder.getCouponInfos()) {
            ShoppingCart cart = new ShoppingCart();
            cart.setProducts(simulationOrder.getProducts());
            cart.setCouponInfos(Lists.newArrayList(couponInfo));
            cart = couponTemplateFactory.getTemplate(cart).calculate(cart);

            Long couponId = couponInfo.getId();
            long orderCost = cart.getCost();
            // 设置当前优惠劵对应的订单价格
            simulationResponse.getCouponToOrderPrice().put(couponId,orderCost);

            // 设置当前最优优惠劵ID
            if (orderCost<minOrderPrice) {
                simulationResponse.setBestCouponId(couponId);
                minOrderPrice=orderCost;
            }
        }
        return simulationResponse;
    }
}
