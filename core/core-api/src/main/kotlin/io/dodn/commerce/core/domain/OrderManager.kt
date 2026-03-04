package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.OrderState
import io.dodn.commerce.core.support.error.CoreException
import io.dodn.commerce.core.support.error.ErrorType
import io.dodn.commerce.storage.db.core.OrderEntity
import io.dodn.commerce.storage.db.core.OrderItemEntity
import io.dodn.commerce.storage.db.core.OrderItemRepository
import io.dodn.commerce.storage.db.core.OrderRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class OrderManager(
    private val orderKeyGenerator: OrderKeyGenerator,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productFinder: ProductFinder,
) {
    @Transactional
    fun create(user: User, newOrder: NewOrder): String {
        val orderProductIds = newOrder.items.map { it.productId }.toSet()
        val productMap = productFinder.findAllActive(orderProductIds)
        if (productMap.isEmpty()) throw CoreException(ErrorType.NOT_FOUND_DATA)
        if (productMap.keys != orderProductIds) throw CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER)

        val order = OrderEntity(
            userId = user.id,
            orderKey = orderKeyGenerator.generate(),
            name = newOrder.items.first().let { productMap[it.productId]!!.name + if (newOrder.items.size > 1) " 외 ${newOrder.items.size - 1}개" else "" },
            totalPrice = newOrder.items.sumOf { productMap[it.productId]!!.price.discountedPrice.multiply(BigDecimal.valueOf(it.quantity)) },
            state = OrderState.CREATED,
        )
        val savedOrder = orderRepository.save(order)

        orderItemRepository.saveAll(
            newOrder.items.map {
                val product = productMap[it.productId]!!
                OrderItemEntity(
                    orderId = savedOrder.id,
                    productId = product.id,
                    productName = product.name,
                    thumbnailUrl = product.thumbnailUrl,
                    shortDescription = product.shortDescription,
                    quantity = it.quantity,
                    unitPrice = product.price.discountedPrice,
                    totalPrice = product.price.discountedPrice.multiply(BigDecimal.valueOf(it.quantity)),
                )
            },
        )

        return savedOrder.orderKey
    }
}
