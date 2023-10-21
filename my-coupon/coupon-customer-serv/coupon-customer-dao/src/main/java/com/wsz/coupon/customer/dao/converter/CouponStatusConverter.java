package com.wsz.coupon.customer.dao.converter;

import com.wsz.coupon.customer.api.enums.CouponStatus;

import javax.persistence.AttributeConverter;

public class CouponStatusConverter implements AttributeConverter<CouponStatus,Integer> {
    // enum转DB value
    @Override
    public Integer convertToDatabaseColumn(CouponStatus couponStatus) {
        return couponStatus.getCode();
    }

    // DB value转enum值
    @Override
    public CouponStatus convertToEntityAttribute(Integer code) {
        return CouponStatus.convert(code);
    }
}
