package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.core.support.OffsetLimit
import io.dodn.commerce.core.support.Page
import io.dodn.commerce.storage.db.core.FavoriteRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class FavoriteReader(
    private val favoriteRepository: FavoriteRepository,
) {
    fun read(user: User, offsetLimit: OffsetLimit): Page<Favorite> {
        val cutoff = LocalDateTime.now().minusDays(FavoritePolicy.RECENT_DAYS)
        val result = favoriteRepository.findByUserIdAndStatusAndUpdatedAtAfter(
            user.id,
            EntityStatus.ACTIVE,
            cutoff,
            offsetLimit.toPageable(),
        )
        return Page(
            result.content.map {
                Favorite(
                    id = it.id,
                    userId = it.userId,
                    productId = it.productId,
                    favoritedAt = it.favoritedAt,
                )
            },
            result.hasNext(),
        )
    }

    fun countRecentByProducts(productIds: List<Long>): Map<Long, Long> {
        val fromDate = LocalDateTime.now().minusDays(FavoritePolicy.RECENT_COUNT_DAYS)
        return favoriteRepository.countByProductIds(productIds, EntityStatus.ACTIVE, fromDate)
            .associate { it.productId to it.count }
    }
}
