package com.wsz.coupon.template.dao.converter;

import com.wsz.coupon.template.api.enums.CouponType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class CouponTypeConverter implements AttributeConverter<CouponType,String> {
    @Override
    public String convertToDatabaseColumn(CouponType couponType) {
        return couponType.getCode();
    }

    @Override
    public CouponType convertToEntityAttribute(String s) {
        return CouponType.convert(s);
    }
}
