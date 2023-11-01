package com.wsz.coupon.customer.feign;

import com.wsz.coupon.customer.feign.fallback.TemplateServiceFallback;
import com.wsz.coupon.customer.feign.fallback.TemplateServiceFallbackFactory;
import com.wsz.coupon.template.api.beans.CouponTemplateInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

@FeignClient(value = "coupon-template-serv",path = "/template",
        // fallback指定降级逻辑
        // fallback = TemplateServiceFallback.class)
        fallbackFactory = TemplateServiceFallbackFactory.class)
public interface TemplateService {
    // 读取优惠劵
    @GetMapping("/getTemplate")
    CouponTemplateInfo getTemplate(@RequestParam("id") Long id);

    // 批量获取
    @GetMapping("/getBatch")
    Map<Long,CouponTemplateInfo> getTemplateInBatch(@RequestParam("ids")Collection<Long> ids);

    @DeleteMapping("/deleteTemplate")
    void deleteTemplate(@RequestParam("id") Long id);
}
