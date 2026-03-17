package io.dodn.commerce.core.domain

import org.springframework.stereotype.Service

@Service
class ProductOptionService(
    private val productOptionFinder: ProductOptionFinder,
) {
    fun findOptions(productId: Long): List<ProductOption> {
        return productOptionFinder.findByProduct(productId)
    }
}
