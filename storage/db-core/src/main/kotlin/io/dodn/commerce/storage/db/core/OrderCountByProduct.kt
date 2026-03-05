package io.dodn.commerce.storage.db.core

interface OrderCountByProduct {
    val productId: Long
    val count: Long
}
