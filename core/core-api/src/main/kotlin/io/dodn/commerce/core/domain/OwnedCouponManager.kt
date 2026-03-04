package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.core.enums.OwnedCouponState
import io.dodn.commerce.core.support.error.CoreException
import io.dodn.commerce.core.support.error.ErrorType
import io.dodn.commerce.storage.db.core.CouponRepository
import io.dodn.commerce.storage.db.core.OwnedCouponEntity
import io.dodn.commerce.storage.db.core.OwnedCouponRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OwnedCouponManager(
    private val ownedCouponRepository: OwnedCouponRepository,
    private val couponRepository: CouponRepository,
) {
    fun download(user: User, couponId: Long) {
        val coupon = couponRepository.findByIdAndStatusAndExpiredAtAfter(couponId, EntityStatus.ACTIVE, LocalDateTime.now())
            ?: throw CoreException(ErrorType.COUPON_NOT_FOUND_OR_EXPIRED)

        val existing = ownedCouponRepository.findByUserIdAndCouponId(user.id, couponId)
        if (existing != null) {
            throw CoreException(ErrorType.COUPON_ALREADY_DOWNLOADED)
        }
        ownedCouponRepository.save(
            OwnedCouponEntity(
                userId = user.id,
                couponId = coupon.id,
                state = OwnedCouponState.DOWNLOADED,
            ),
        )
    }
}
