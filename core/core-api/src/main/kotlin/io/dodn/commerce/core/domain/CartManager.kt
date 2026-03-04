package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.core.support.error.CoreException
import io.dodn.commerce.core.support.error.ErrorType
import io.dodn.commerce.storage.db.core.CartItemEntity
import io.dodn.commerce.storage.db.core.CartItemRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CartManager(
    private val cartItemRepository: CartItemRepository,
) {
    @Transactional
    fun add(user: User, item: AddCartItem): Long {
        return cartItemRepository.findByUserIdAndProductId(user.id, item.productId)?.apply {
            if (isDeleted()) active()
            applyQuantity(item.quantity)
        }?.id ?: cartItemRepository.save(
            CartItemEntity(
                userId = user.id,
                productId = item.productId,
                quantity = item.quantity,
            ),
        ).id
    }

    @Transactional
    fun modify(user: User, item: ModifyCartItem): Long {
        val found = cartItemRepository.findByUserIdAndIdAndStatus(user.id, item.cartItemId, EntityStatus.ACTIVE)
            ?: throw CoreException(ErrorType.NOT_FOUND_DATA)
        found.applyQuantity(item.quantity)
        return found.id
    }

    @Transactional
    fun delete(user: User, cartItemId: Long) {
        val entity = cartItemRepository.findByUserIdAndIdAndStatus(user.id, cartItemId, EntityStatus.ACTIVE)
            ?: throw CoreException(ErrorType.NOT_FOUND_DATA)
        entity.delete()
    }
}
