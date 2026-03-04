package io.dodn.commerce.core.domain

import org.springframework.stereotype.Service

@Service
class CancelService(
    private val cancelManager: CancelManager,
) {
    fun cancel(user: User, action: CancelAction): Long = cancelManager.cancel(user, action)
}
