package com.wsz.coupon.calculation.template.impl;

import com.wsz.coupon.calculation.template.AbstractRuleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MoneyOffTemplate extends AbstractRuleTemplate {
    @Override
    protected Long calculateNewPrice(Long orderTotalAmount, Long shopTotalAmount, Long quota) {
        // benefitAmount为扣减价格
        Long benefitAmount = shopTotalAmount<quota ? shopTotalAmount: quota;
        return orderTotalAmount-benefitAmount;
    }
}
