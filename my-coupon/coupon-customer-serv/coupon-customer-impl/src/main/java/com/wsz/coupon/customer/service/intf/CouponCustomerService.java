package com.wsz.coupon.customer.service.intf;

import com.wsz.coupon.calculation.api.beans.ShoppingCart;
import com.wsz.coupon.calculation.api.beans.SimulationOrder;
import com.wsz.coupon.calculation.api.beans.SimulationResponse;
import com.wsz.coupon.customer.api.beans.RequestCoupon;
import com.wsz.coupon.customer.api.beans.SearchCoupon;
import com.wsz.coupon.customer.dao.entity.Coupon;
import com.wsz.coupon.template.api.beans.CouponInfo;

import java.util.List;

// 用户对接服务
public interface CouponCustomerService {

    // 领券接口
    Coupon requestCoupon(RequestCoupon request);

    // 核销优惠券
    ShoppingCart placeOrder(ShoppingCart info);

    // 优惠券金额试算
    SimulationResponse simulateOrderPrice(SimulationOrder order);

    void deleteCoupon(Long userId, Long couponId);

    // 查询用户优惠券
    List<CouponInfo> findCoupon(SearchCoupon request);

    void deleteCouponTemplate(Long templateId);
}
