package io.dodn.commerce.storage.db.core

import io.dodn.commerce.core.enums.EntityStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ProductOptionRepository : JpaRepository<ProductOptionEntity, Long> {
    fun findByProductIdAndStatus(productId: Long, status: EntityStatus): List<ProductOptionEntity>
}
