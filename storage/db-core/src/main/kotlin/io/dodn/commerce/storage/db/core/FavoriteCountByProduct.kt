package io.dodn.commerce.storage.db.core

interface FavoriteCountByProduct {
    val productId: Long
    val count: Long
}
