package io.dodn.commerce.core.api.assembler

import io.dodn.commerce.core.api.controller.v1.response.ProductDetailResponse
import io.dodn.commerce.core.api.controller.v1.response.ProductResponse
import io.dodn.commerce.core.domain.CouponService
import io.dodn.commerce.core.domain.FavoriteService
import io.dodn.commerce.core.domain.OrderService
import io.dodn.commerce.core.domain.ProductOptionService
import io.dodn.commerce.core.domain.ProductSectionService
import io.dodn.commerce.core.domain.ProductService
import io.dodn.commerce.core.domain.ReviewService
import io.dodn.commerce.core.domain.ReviewTarget
import io.dodn.commerce.core.enums.ReviewTargetType
import io.dodn.commerce.core.support.OffsetLimit
import io.dodn.commerce.core.support.response.PageResponse
import org.springframework.stereotype.Component

@Component
class ProductAssembler(
    private val productService: ProductService,
    private val productSectionService: ProductSectionService,
    private val reviewService: ReviewService,
    private val couponService: CouponService,
    private val favoriteService: FavoriteService,
    private val orderService: OrderService,
    private val productOptionService: ProductOptionService,
) {
    fun findProducts(categoryId: Long, offsetLimit: OffsetLimit): PageResponse<ProductResponse> {
        val products = productService.findProducts(categoryId, offsetLimit)
        val productIds = products.content.map { it.id }
        val favoriteCounts = favoriteService.countRecentByProducts(productIds)
        val orderCounts = orderService.countRecentByProducts(productIds)
        return PageResponse(ProductResponse.of(products.content, favoriteCounts, orderCounts), products.hasNext)
    }

    fun findDetail(productId: Long): ProductDetailResponse {
        val product = productService.findProduct(productId)
        val sections = productSectionService.findSections(productId)
        val rateSummary = reviewService.findRateSummary(ReviewTarget(ReviewTargetType.PRODUCT, productId))
        val coupons = couponService.getCouponsForProducts(listOf(productId))
        val options = productOptionService.findOptions(productId)
        return ProductDetailResponse.of(product, sections, rateSummary, coupons, options)
    }
}
