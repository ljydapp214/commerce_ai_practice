package io.dodn.commerce.core.api.controller.v1.response

import io.dodn.commerce.core.domain.PointBalance
import io.dodn.commerce.core.domain.PointHistory
import java.math.BigDecimal

data class PointResponse(
    val userId: Long,
    val balance: BigDecimal,
    val histories: List<PointHistoryResponse>,
) {
    companion object {
        fun of(balance: PointBalance, histories: List<PointHistory>): PointResponse {
            return PointResponse(
                userId = balance.userId,
                balance = balance.balance,
                histories = histories.map {
                    PointHistoryResponse(
                        type = it.type,
                        amount = it.amount,
                        appliedAt = it.appliedAt,
                    )
                },
            )
        }
    }
}
