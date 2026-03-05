package io.dodn.commerce.core.domain

import io.dodn.commerce.core.support.OffsetLimit
import io.dodn.commerce.core.support.Page
import org.springframework.stereotype.Service

@Service
class FavoriteService(
    private val favoriteReader: FavoriteReader,
    private val favoriteManager: FavoriteManager,
) {
    fun findFavorites(user: User, offsetLimit: OffsetLimit): Page<Favorite> = favoriteReader.read(user, offsetLimit)

    fun addFavorite(user: User, productId: Long): Long = favoriteManager.add(user, productId)

    fun removeFavorite(user: User, productId: Long): Long = favoriteManager.remove(user, productId)

    fun countRecentByProducts(productIds: List<Long>): Map<Long, Long> {
        if (productIds.isEmpty()) return emptyMap()
        return favoriteReader.countRecentByProducts(productIds)
    }
}
