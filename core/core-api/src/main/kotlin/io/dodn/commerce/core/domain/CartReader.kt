package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.storage.db.core.CartItemRepository
import io.dodn.commerce.storage.db.core.ProductRepository
import org.springframework.stereotype.Component

@Component
class CartReader(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) {
    fun read(user: User): Cart {
        val items = cartItemRepository.findByUserIdAndStatus(user.id, EntityStatus.ACTIVE)
        val productMap = productRepository.findAllById(items.map { it.productId })
            .associateBy { it.id }

        return Cart(
            userId = user.id,
            items = items.filter { productMap.containsKey(it.productId) }
                .map {
                    CartItem(
                        id = it.id,
                        product = Product(
                            id = productMap[it.productId]!!.id,
                            name = productMap[it.productId]!!.name,
                            thumbnailUrl = productMap[it.productId]!!.thumbnailUrl,
                            description = productMap[it.productId]!!.description,
                            shortDescription = productMap[it.productId]!!.shortDescription,
                            price = Price(
                                costPrice = productMap[it.productId]!!.costPrice,
                                salesPrice = productMap[it.productId]!!.salesPrice,
                                discountedPrice = productMap[it.productId]!!.discountedPrice,
                            ),
                        ),
                        quantity = it.quantity,
                    )
                },
        )
    }
}
