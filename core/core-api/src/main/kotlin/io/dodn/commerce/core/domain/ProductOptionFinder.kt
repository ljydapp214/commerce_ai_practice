package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.storage.db.core.ProductOptionRepository
import org.springframework.stereotype.Component

@Component
class ProductOptionFinder(
    private val productOptionRepository: ProductOptionRepository,
) {
    fun findByProduct(productId: Long): List<ProductOption> {
        return productOptionRepository.findByProductIdAndStatus(productId, EntityStatus.ACTIVE)
            .map {
                ProductOption(
                    id = it.id,
                    productId = it.productId,
                    name = it.name,
                    description = it.description,
                    price = Price(
                        costPrice = it.costPrice,
                        salesPrice = it.salesPrice,
                        discountedPrice = it.discountedPrice,
                    ),
                )
            }
    }
}
