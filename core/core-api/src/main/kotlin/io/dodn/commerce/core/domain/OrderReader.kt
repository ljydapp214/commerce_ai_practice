package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.core.enums.OrderState
import io.dodn.commerce.core.support.error.CoreException
import io.dodn.commerce.core.support.error.ErrorType
import io.dodn.commerce.storage.db.core.OrderItemRepository
import io.dodn.commerce.storage.db.core.OrderRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderReader(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
) {
    fun readAll(user: User): List<OrderSummary> {
        val orders = orderRepository.findByUserIdAndStateAndStatusOrderByIdDesc(user.id, OrderState.PAID, EntityStatus.ACTIVE)
        if (orders.isEmpty()) return emptyList()

        return orders.map {
            OrderSummary(
                id = it.id,
                key = it.orderKey,
                name = it.name,
                userId = user.id,
                totalPrice = it.totalPrice,
                state = it.state,
            )
        }
    }

    @Transactional
    fun read(user: User, orderKey: String, state: OrderState): Order {
        val order = orderRepository.findByOrderKeyAndStateAndStatus(orderKey, state, EntityStatus.ACTIVE) ?: throw CoreException(ErrorType.NOT_FOUND_DATA)
        if (order.userId != user.id) throw CoreException(ErrorType.NOT_FOUND_DATA)

        val items = orderItemRepository.findByOrderId(order.id)
        if (items.isEmpty()) throw CoreException(ErrorType.NOT_FOUND_DATA)

        return Order(
            id = order.id,
            key = order.orderKey,
            name = order.name,
            userId = user.id,
            totalPrice = order.totalPrice,
            state = order.state,
            items = items.map {
                OrderItem(
                    orderId = order.id,
                    productId = it.productId,
                    productName = it.productName,
                    thumbnailUrl = it.thumbnailUrl,
                    shortDescription = it.shortDescription,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    totalPrice = it.totalPrice,
                )
            },
        )
    }
}
