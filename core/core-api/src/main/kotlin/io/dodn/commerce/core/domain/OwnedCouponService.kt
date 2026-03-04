package io.dodn.commerce.core.domain

import org.springframework.stereotype.Service

@Service
class OwnedCouponService(
    private val ownedCouponReader: OwnedCouponReader,
    private val ownedCouponManager: OwnedCouponManager,
) {
    fun getOwnedCoupons(user: User): List<OwnedCoupon> = ownedCouponReader.readAll(user)

    fun download(user: User, couponId: Long) = ownedCouponManager.download(user, couponId)

    fun getOwnedCouponsForCheckout(user: User, productIds: Collection<Long>): List<OwnedCoupon> = ownedCouponReader.readForCheckout(user, productIds)
}
