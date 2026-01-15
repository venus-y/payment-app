package im.bigs.pg.application.payment.`in`

import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.`in`.QueryResult
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.domain.payment.PaymentSummary
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaymentPortModelsTest {
    @Test
    @DisplayName("PaymentCommand 기본값이 올바르게 설정된다")
    fun `payment command defaults`() {
        val command = PaymentCommand(partnerId = 1L, amount = BigDecimal("1000"))

        assertEquals(1L, command.partnerId)
        assertEquals(BigDecimal("1000"), command.amount)
        assertNull(command.cardBin)
        assertNull(command.cardLast4)
        assertNull(command.productName)
    }

    @Test
    @DisplayName("QueryFilter 기본값이 올바르게 설정된다")
    fun `query filter defaults`() {
        val filter = QueryFilter()

        assertEquals(20, filter.limit)
        assertNull(filter.partnerId)
        assertNull(filter.status)
        assertNull(filter.from)
        assertNull(filter.to)
        assertNull(filter.cursor)
    }

    @Test
    @DisplayName("QueryResult는 항목/통계/커서 값을 보존한다")
    fun `query result holds values`() {
        val now = LocalDateTime.of(2024, 1, 1, 0, 0)
        val payment = Payment(
            id = 1L,
            partnerId = 1L,
            amount = BigDecimal("1000"),
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = BigDecimal("30"),
            netAmount = BigDecimal("970"),
            cardBin = null,
            cardLast4 = "4242",
            approvalCode = "APPROVAL",
            approvedAt = now,
            status = PaymentStatus.APPROVED,
            createdAt = now,
            updatedAt = now,
        )
        val summary = PaymentSummary(1, BigDecimal("1000"), BigDecimal("970"))

        val result = QueryResult(
            items = listOf(payment),
            summary = summary,
            nextCursor = "cursor",
            hasNext = true,
        )

        assertEquals(1, result.items.size)
        assertEquals(summary, result.summary)
        assertEquals("cursor", result.nextCursor)
        assertEquals(true, result.hasNext)
    }
}