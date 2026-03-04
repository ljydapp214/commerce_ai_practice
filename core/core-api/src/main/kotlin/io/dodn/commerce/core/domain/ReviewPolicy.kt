package io.dodn.commerce.core.domain

import java.math.BigDecimal

object ReviewPolicy {
    const val ELIGIBLE_ORDER_DAYS = 14L
    const val UPDATE_EXPIRE_DAYS = 7L
    val MAX_RATE: BigDecimal = BigDecimal.valueOf(5.0)
}
