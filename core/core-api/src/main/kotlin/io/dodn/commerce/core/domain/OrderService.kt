package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.OrderState
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderReader: OrderReader,
    private val orderManager: OrderManager,
) {
    fun create(user: User, newOrder: NewOrder): String = orderManager.create(user, newOrder)

    fun getOrders(user: User): List<OrderSummary> = orderReader.readAll(user)

    fun getOrder(user: User, orderKey: String, state: OrderState): Order = orderReader.read(user, orderKey, state)
}
