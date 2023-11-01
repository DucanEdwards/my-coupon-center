package com.wsz.coupon.customer.feign.fallback;

import com.google.common.collect.Maps;
import com.wsz.coupon.customer.feign.TemplateService;
import com.wsz.coupon.template.api.beans.CouponTemplateInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class TemplateServiceFallbackFactory implements FallbackFactory<TemplateService> {
    // 降低方法你可以捕捉到原始请求的具体报错异常cause
    @Override
    public TemplateService create(Throwable cause) {
        return new TemplateService() {

            @Override
            public CouponTemplateInfo getTemplate(Long id) {
                log.info("fallback factory method test" + cause);
                return null;
            }

            @Override
            public Map<Long, CouponTemplateInfo> getTemplateInBatch(Collection<Long> ids) {
                log.info("fallback factory method test" + cause);
                return Maps.newHashMap();
            }

            @Override
            public void deleteTemplate(Long id) {
                log.info("fallback factory method deleteTemplate" + cause);
            }
        };
    }
}
