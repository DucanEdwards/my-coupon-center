package com.wsz.coupon.customer.dao;

import org.springframework.data.jpa.repository.JpaRepository;


public interface CouponDao extends JpaRepository<CouponDao,Long> {
    long countByUserIdAndTemplateId(Long userId,Long templateId);
}
