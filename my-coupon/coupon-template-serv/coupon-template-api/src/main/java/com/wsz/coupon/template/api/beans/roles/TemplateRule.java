package com.wsz.coupon.template.api.beans.roles;

/**
 * 优惠劵计算规则
 */
public class TemplateRule {
    // 享受的折扣
    private Discount discount;

    // 每人最多领劵数
    private Integer limitation;

    // 过期时间
    private Long deadline;
}
