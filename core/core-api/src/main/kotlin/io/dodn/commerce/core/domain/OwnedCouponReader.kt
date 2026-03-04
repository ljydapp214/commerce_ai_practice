package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.storage.db.core.CouponRepository
import io.dodn.commerce.storage.db.core.OwnedCouponRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OwnedCouponReader(
    private val ownedCouponRepository: OwnedCouponRepository,
    private val couponRepository: CouponRepository,
) {
    fun readAll(user: User): List<OwnedCoupon> {
        val ownedCoupons = ownedCouponRepository.findByUserIdAndStatus(user.id, EntityStatus.ACTIVE)
        if (ownedCoupons.isEmpty()) return emptyList()
        val couponMap = couponRepository.findAllById(ownedCoupons.map { it.couponId }.toSet())
            .associateBy { it.id }

        return ownedCoupons.map {
            OwnedCoupon(
                id = it.id,
                userId = it.userId,
                state = it.state,
                coupon = Coupon(
                    id = couponMap[it.couponId]!!.id,
                    name = couponMap[it.couponId]!!.name,
                    type = couponMap[it.couponId]!!.type,
                    discount = couponMap[it.couponId]!!.discount,
                    expiredAt = couponMap[it.couponId]!!.expiredAt,
                ),
            )
        }
    }

    fun readForCheckout(user: User, productIds: Collection<Long>): List<OwnedCoupon> {
        if (productIds.isEmpty()) return emptyList()
        val applicableCouponMap = couponRepository.findApplicableCouponIds(productIds)
            .associateBy { it.id }

        if (applicableCouponMap.isEmpty()) return emptyList()
        val ownedCoupons = ownedCouponRepository.findOwnedCouponIds(user.id, applicableCouponMap.keys, LocalDateTime.now())

        if (ownedCoupons.isEmpty()) return emptyList()
        return ownedCoupons.map {
            OwnedCoupon(
                id = it.id,
                userId = it.userId,
                state = it.state,
                coupon = Coupon(
                    id = applicableCouponMap[it.couponId]!!.id,
                    name = applicableCouponMap[it.couponId]!!.name,
                    type = applicableCouponMap[it.couponId]!!.type,
                    discount = applicableCouponMap[it.couponId]!!.discount,
                    expiredAt = applicableCouponMap[it.couponId]!!.expiredAt,
                ),
            )
        }
    }
}
