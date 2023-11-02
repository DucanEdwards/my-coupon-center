package com.wsz.coupon.template.service.intf;


import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

// 为了方便演示，新定义了一个接口类
@LocalTCC
public interface CouponTemplateServiceTCC extends CouponTemplateService {

    @TwoPhaseBusinessAction(
            name = "deleteTemplateTCC",
            commitMethod = "deleteTemplateCommit",
            rollbackMethod = "deleteTemplateCancel"
    )
    void deleteTemplateTCC(@BusinessActionContextParameter(paramName = "id") Long id);

    void deleteTemplateCommit(BusinessActionContext context);

    void deleteTemplateCancel(BusinessActionContext context);
}
