package com.wsz.coupon.template.dao.converter;

import com.alibaba.fastjson.JSON;
import com.wsz.coupon.template.api.beans.roles.TemplateRule;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class RuleConverter implements AttributeConverter<TemplateRule,String> {
    @Override
    public String convertToDatabaseColumn(TemplateRule templateRule) {
        return JSON.toJSONString(templateRule);
    }

    @Override
    public TemplateRule convertToEntityAttribute(String s) {
        return JSON.parseObject(s,TemplateRule.class);
    }
}
