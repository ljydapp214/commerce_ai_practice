package io.dodn.commerce.core.domain

import io.dodn.commerce.core.support.error.CoreException
import io.dodn.commerce.core.support.error.ErrorType
import io.dodn.commerce.storage.db.core.FavoriteEntity
import io.dodn.commerce.storage.db.core.FavoriteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class FavoriteManager(
    private val favoriteRepository: FavoriteRepository,
) {
    @Transactional
    fun add(user: User, productId: Long): Long {
        val existing = favoriteRepository.findByUserIdAndProductId(user.id, productId)
        return if (existing == null) {
            val saved = favoriteRepository.save(
                FavoriteEntity(
                    userId = user.id,
                    productId = productId,
                    favoritedAt = LocalDateTime.now(),
                ),
            )
            saved.id
        } else {
            existing.favorite()
            existing.id
        }
    }

    @Transactional
    fun remove(user: User, productId: Long): Long {
        val existing = favoriteRepository.findByUserIdAndProductId(user.id, productId)
            ?: throw CoreException(ErrorType.NOT_FOUND_DATA)
        existing.delete()
        return existing.id
    }
}
