package com.wsz.coupon.customer.service;

import com.wsz.coupon.customer.dao.entity.Coupon;
import com.wsz.coupon.template.api.beans.CouponInfo;

public class CouponConverter {

    public static CouponInfo convertToCoupon(Coupon coupon) {

        return CouponInfo.builder()
                .id(coupon.getId())
                .status(coupon.getStatus().getCode())
                .templateId(coupon.getTemplateId())
                .shopId(coupon.getShopId())
                .userId(coupon.getUserId())
                .template(coupon.getTemplateInfo())
                .build();
    }
}
