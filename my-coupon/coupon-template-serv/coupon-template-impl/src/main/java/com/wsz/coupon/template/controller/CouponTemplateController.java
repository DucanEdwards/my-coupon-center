package com.wsz.coupon.template.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.wsz.coupon.template.api.beans.CouponTemplateInfo;
import com.wsz.coupon.template.api.beans.PagedCouponTemplateInfo;
import com.wsz.coupon.template.api.beans.TemplateSearchParams;
import com.wsz.coupon.template.service.intf.CouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/template")
public class CouponTemplateController {

    @Autowired
    private CouponTemplateService couponTemplateService;

    // 创建优惠券
    @PostMapping("/addTemplate")
    public CouponTemplateInfo addTemplate(@Valid @RequestBody CouponTemplateInfo request) {
        log.info("Create coupon template: data={}", request);
        return couponTemplateService.createTemplate(request);
    }

    @PostMapping("/cloneTemplate")
    public CouponTemplateInfo cloneTemplate(@RequestParam("id") Long templateId) {
        log.info("Clone coupon template: data={}", templateId);
        return couponTemplateService.cloneTemplate(templateId);
    }

    // 读取优惠券
    @GetMapping("/getTemplate")
    @SentinelResource(value = "getTemplate")
    public CouponTemplateInfo getTemplate(@RequestParam("id") Long id){
        log.info("Load template, id={}", id);
        return couponTemplateService.loadTemplateInfo(id);
    }

    // 批量获取
    @GetMapping("/getBatch")
    @SentinelResource(value = "getTemplateInBatch",
            fallback = "getTemplateInBatch_fallback",
            // 适用于BlockException，如果和fallback同时使用，在抛出BlockException时优先使用blockHandler指定的方法
            blockHandler = "getTemplateInBatch_block"
    )
    public Map<Long, CouponTemplateInfo> getTemplateInBatch(@RequestParam("ids") Collection<Long> ids) {
        log.info("getTemplateInBatch: {}", JSON.toJSONString(ids));
        return couponTemplateService.getTemplateInfoMap(ids);
    }

    // 接口被降级时的方法
    // 大多数执行的是静默逻辑，尽可能返回一个可被服务处理的默认值
    public Map<Long, CouponTemplateInfo> getTemplateInBatch_fallback(Collection<Long> ids) {
        log.info("接口被降级");
        return Maps.newHashMap();
    }

    /**
     * 流控被降级的方法
     * blockHandler只在服务抛出BlockException后才执行降级逻辑
     * @param ids
     * @param exception
     * @return
     */
    public Map<Long, CouponTemplateInfo> getTemplateInBatch_block(Collection<Long> ids, BlockException exception) {
        log.info("接口被限流");
        return Maps.newHashMap();
    }

    // 搜索模板
    @PostMapping("/search")
    public PagedCouponTemplateInfo search(@Valid @RequestBody TemplateSearchParams request) {
        log.info("search templates, payload={}", request);
        return couponTemplateService.search(request);
    }

    // 优惠券无效化
    @DeleteMapping("/deleteTemplate")
    public void deleteTemplate(@RequestParam("id") Long id){
        log.info("Load template, id={}", id);
        couponTemplateService.deleteTemplate(id);
    }
}
