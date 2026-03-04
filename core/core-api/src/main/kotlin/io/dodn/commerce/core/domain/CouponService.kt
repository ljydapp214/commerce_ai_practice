package io.dodn.commerce.core.domain

import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponReader: CouponReader,
) {
    fun getCouponsForProducts(productIds: Collection<Long>): List<Coupon> = couponReader.readForProducts(productIds)
}
