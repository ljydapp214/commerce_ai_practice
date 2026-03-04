package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.SettlementState
import io.dodn.commerce.storage.db.core.SettlementEntity
import io.dodn.commerce.storage.db.core.SettlementRepository
import io.dodn.commerce.storage.db.core.SettlementTargetRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Component
class SettlementManager(
    private val settlementTargetRepository: SettlementTargetRepository,
    private val settlementRepository: SettlementRepository,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun calculate(settleDate: LocalDate): Int {
        val summary = settlementTargetRepository.findSummary(settleDate)
        val settlements = summary.map {
            val amount = SettlementCalculator.calculate(it.targetAmount)
            SettlementEntity(
                merchantId = it.merchantId,
                settlementDate = it.settlementDate,
                originalAmount = amount.originalAmount,
                feeAmount = amount.feeAmount,
                feeRate = amount.feeRate,
                settlementAmount = amount.settlementAmount,
                state = SettlementState.READY,
            )
        }
        settlementRepository.saveAll(settlements)
        return settlements.size
    }

    fun transfer(): Int {
        val settlements = settlementRepository.findByState(SettlementState.READY)
            .groupBy { it.merchantId }

        for (settlement in settlements) {
            try {
                val transferAmount = settlement.value.sumOf { it.settlementAmount }
                if (transferAmount <= BigDecimal.ZERO) {
                    // NOTE: 총 정산금이 음수라면 돈 보낼 필요가 없다는 것이므로, 정산금이 양수가 될 때까지 스킵
                    log.warn("[SETTLEMENT_TRANSFER] {} 가맹점 미정산 금액 : {} 발생 확인 요망!", settlement.key, transferAmount)
                    continue
                }

                /**
                 * NOTE: 외부 펌 등 이체 서비스 API 호출
                 */

                settlement.value.forEach { it.sent() }
                settlementRepository.saveAll(settlement.value)
            } catch (e: Exception) {
                log.error("[SETTLEMENT_TRANSFER] {} 가맹점 정산 중 에러 발생: {}", settlement.key, e.message, e)
            }
        }
        return settlements.size
    }
}
