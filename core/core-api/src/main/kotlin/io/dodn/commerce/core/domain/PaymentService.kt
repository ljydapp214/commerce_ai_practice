package io.dodn.commerce.core.domain

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PaymentService(
    private val paymentManager: PaymentManager,
) {
    fun createPayment(order: Order, paymentDiscount: PaymentDiscount): Long = paymentManager.create(order, paymentDiscount)

    fun success(orderKey: String, externalPaymentKey: String, amount: BigDecimal): Long = paymentManager.processSuccess(orderKey, externalPaymentKey, amount)

    fun fail(orderKey: String, code: String, message: String) = paymentManager.processFail(orderKey, code, message)
}
