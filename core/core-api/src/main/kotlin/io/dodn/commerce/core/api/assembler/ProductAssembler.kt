package io.dodn.commerce.core.api.assembler

import io.dodn.commerce.core.api.controller.v1.response.ProductDetailResponse
import io.dodn.commerce.core.domain.CouponService
import io.dodn.commerce.core.domain.ProductSectionService
import io.dodn.commerce.core.domain.ProductService
import io.dodn.commerce.core.domain.ReviewService
import io.dodn.commerce.core.domain.ReviewTarget
import io.dodn.commerce.core.enums.ReviewTargetType
import org.springframework.stereotype.Component

@Component
class ProductAssembler(
    private val productService: ProductService,
    private val productSectionService: ProductSectionService,
    private val reviewService: ReviewService,
    private val couponService: CouponService,
) {
    fun findDetail(productId: Long): ProductDetailResponse {
        val product = productService.findProduct(productId)
        val sections = productSectionService.findSections(productId)
        val rateSummary = reviewService.findRateSummary(ReviewTarget(ReviewTargetType.PRODUCT, productId))
        val coupons = couponService.getCouponsForProducts(listOf(productId))
        return ProductDetailResponse.of(product, sections, rateSummary, coupons)
    }
}
