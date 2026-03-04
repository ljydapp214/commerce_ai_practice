package io.dodn.commerce.core.api.controller.v1.response

import io.dodn.commerce.core.enums.PointType
import java.math.BigDecimal
import java.time.LocalDateTime

data class PointHistoryResponse(
    val type: PointType,
    val amount: BigDecimal,
    val appliedAt: LocalDateTime,
)
