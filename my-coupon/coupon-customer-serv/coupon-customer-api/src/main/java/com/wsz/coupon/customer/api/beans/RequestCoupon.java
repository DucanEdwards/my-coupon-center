package com.wsz.coupon.customer.api.beans;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCoupon {
    // 用户领劵
    @NotNull
    private long userId;

    // 劵模版ID
    @NotNull
    private Long couponTemplateId;
}
