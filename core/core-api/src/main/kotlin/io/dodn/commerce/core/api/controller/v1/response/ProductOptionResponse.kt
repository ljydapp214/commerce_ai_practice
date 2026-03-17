package io.dodn.commerce.core.api.controller.v1.response

import io.dodn.commerce.core.domain.ProductOption
import java.math.BigDecimal

data class ProductOptionResponse(
    val id: Long,
    val name: String,
    val description: String,
    val costPrice: BigDecimal,
) {
    companion object {
        fun of(option: ProductOption): ProductOptionResponse = ProductOptionResponse(
            id = option.id,
            name = option.name,
            description = option.description,
            costPrice = option.price,
        )
    }
}
