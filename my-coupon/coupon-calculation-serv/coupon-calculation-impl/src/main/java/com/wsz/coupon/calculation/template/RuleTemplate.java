package com.wsz.coupon.calculation.template;

import com.wsz.coupon.calculation.api.beans.ShoppingCart;

public interface RuleTemplate {
    // 计算优惠券
    ShoppingCart calculate(ShoppingCart settlement);
}
