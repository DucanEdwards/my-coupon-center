package com.wsz.coupon.template.api.beans.roles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠劵计算规则
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRule {
    // 享受的折扣
    private Discount discount;

    // 每人最多领劵数
    private Integer limitation;

    // 过期时间
    private Long deadline;
}
