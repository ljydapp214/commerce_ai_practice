package io.dodn.commerce.core.domain

import java.math.BigDecimal

data class ProductOption(
    val id: Long,
    val productId: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
)
