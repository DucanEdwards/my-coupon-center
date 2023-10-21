package com.wsz.coupon.calculation.template;

import com.wsz.coupon.calculation.api.beans.ShoppingCart;
import com.wsz.coupon.calculation.template.impl.*;
import com.wsz.coupon.template.api.beans.CouponTemplateInfo;
import com.wsz.coupon.template.api.enums.CouponType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;

/**
 * 工厂方法根据订单中的优惠劵信息，返回对应template的用于计算优惠价
 */
@Slf4j
@Component
public class CouponTemplateFactory {
    @Autowired
    private MoneyOffTemplate moneyOffTemplate;

    @Autowired
    private DiscountTemplate discountTemplate;

    @Autowired
    private RandomReductionTemplate randomReductionTemplate;

    @Autowired
    private LonelyNightTemplate lonelyNightTemplate;

    @Autowired
    private DummyTemplate dummyTemplate;

    public RuleTemplate getTemplate(ShoppingCart order){
        // 不使用优惠劵
        if (CollectionUtils.isEmpty(order.getCouponInfos())) {
            // 不进行优惠计算
            return dummyTemplate;
        }
        // 获取优惠劵类型，仅支持单张优惠劵
        CouponTemplateInfo template = order.getCouponInfos().get(0).getTemplate();
        CouponType couponType = CouponType.convert(template.getType());

        switch (couponType){
            // 订单满减券
            case MONEY_OFF:
                return moneyOffTemplate;
            // 随机立减券
            case RANDOM_DISCOUNT:
                return randomReductionTemplate;
            // 午夜下单优惠翻倍
            case LONELY_NIGHT_MONEY_OFF:
                return lonelyNightTemplate;
            // 打折券
            case DISCOUNT:
                return discountTemplate;
            // 未知类型的券模板
            default:
                return dummyTemplate;
        }
    }


}
