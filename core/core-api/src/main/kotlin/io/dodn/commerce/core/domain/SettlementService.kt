package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.PaymentState
import io.dodn.commerce.core.enums.TransactionType
import io.dodn.commerce.storage.db.core.CancelRepository
import io.dodn.commerce.storage.db.core.PaymentRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SettlementService(
    private val paymentRepository: PaymentRepository,
    private val cancelRepository: CancelRepository,
    private val settlementTargetLoader: SettlementTargetLoader,
    private val settlementManager: SettlementManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun loadTargets(settleDate: LocalDate, from: LocalDateTime, to: LocalDateTime) {
        var paymentPageable: Pageable = PageRequest.of(0, SettlementPolicy.BATCH_PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"))
        do {
            val payments = paymentRepository.findAllByStateAndPaidAtBetween(PaymentState.SUCCESS, from, to, paymentPageable)
            try {
                settlementTargetLoader.process(settleDate, TransactionType.PAYMENT, payments.content.associate { it.orderId to it.id })
            } catch (e: Exception) {
                log.error("[SETTLEMENT_LOAD_TARGETS] `결제` 거래건 정산 대상 생성 중 오류 발생 offset: {} size: {} page: {} error: {}", paymentPageable.offset, paymentPageable.pageSize, paymentPageable.pageNumber, e.message, e)
            }
            paymentPageable = payments.nextPageable()
        } while (payments.hasNext())

        var cancelPageable: Pageable = PageRequest.of(0, SettlementPolicy.BATCH_PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"))
        do {
            val cancels = cancelRepository.findAllByCanceledAtBetween(from, to, cancelPageable)
            try {
                settlementTargetLoader.process(settleDate, TransactionType.CANCEL, cancels.content.associate { it.orderId to it.id })
            } catch (e: Exception) {
                log.error("[SETTLEMENT_LOAD_TARGETS] `취소` 거래건 정산 대상 생성 중 오류 발생 offset: {} size: {} page: {} error: {}", cancelPageable.offset, cancelPageable.pageSize, cancelPageable.pageNumber, e.message, e)
            }
            cancelPageable = cancels.nextPageable()
        } while (cancels.hasNext())
    }

    fun calculate(settleDate: LocalDate): Int = settlementManager.calculate(settleDate)

    fun transfer(): Int = settlementManager.transfer()
}
