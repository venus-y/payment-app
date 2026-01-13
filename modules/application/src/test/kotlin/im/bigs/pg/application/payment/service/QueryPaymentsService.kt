package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.*
import im.bigs.pg.domain.payment.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class QueryPaymentsServiceTest {

    @MockK
    lateinit var paymentOutPort: PaymentOutPort

    lateinit var service: QueryPaymentsService

    @BeforeEach
    fun setUp() {
        service = QueryPaymentsService(paymentOutPort)
    }

    @Test
    fun `정상 조회 시 nextCursor 와 hasNext 가 계산된다`() {
        // given
        val now = LocalDateTime.of(2024, 1, 1, 12, 0)

        every { paymentOutPort.findBy(any()) } returns PaymentPage(
            items = listOf(
                mockPayment(2, now),
                mockPayment(1, now.minusMinutes(1)),
            ),
            hasNext = true,
            nextCursorCreatedAt = now.minusMinutes(1),
            nextCursorId = 1L,
        )

        every { paymentOutPort.summary(any()) } returns PaymentSummaryProjection(
            count = 10,
            totalAmount = BigDecimal.valueOf(100_000),
            totalNetAmount = BigDecimal.valueOf(97_000),
        )

        // when
        val result = service.query(QueryFilter(limit = 2))

        // then
        assertEquals(2, result.items.size)
        assertTrue(result.hasNext)
        assertNotNull(result.nextCursor)
        assertEquals(10, result.summary.count)

        verify(exactly = 1) { paymentOutPort.findBy(any()) }
        verify(exactly = 1) { paymentOutPort.summary(any()) }
    }

    @Test
    fun `잘못된 status 는 null 로 처리되어 필터 없이 조회된다`() {
        // given
        every { paymentOutPort.findBy(match { it.status == null }) } returns emptyPage()
        every { paymentOutPort.summary(match { it.status == null }) } returns emptySummary()

        // when
        val result = service.query(QueryFilter(status = "wrongValue"))

        // then
        assertTrue(result.items.isEmpty())
        assertEquals(0, result.summary.count)
    }

    @Test
    fun `잘못된 커서는 무시되고 첫 페이지로 조회된다`() {
        // given
        every { paymentOutPort.findBy(match { it.cursorCreatedAt == null }) } returns emptyPage()
        every { paymentOutPort.summary(any()) } returns emptySummary()

        // when
        service.query(QueryFilter(cursor = "invalidCursor"))

        // then
        verify { paymentOutPort.findBy(any()) }
    }

    /* ---------- test helpers ---------- */

    private fun mockPayment(id: Long, createdAt: LocalDateTime) =
        Payment(
            id = id,
            partnerId = 1L,
            amount = BigDecimal.TEN,
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = BigDecimal("0.3"),
            netAmount = BigDecimal("9.7"),
            approvalCode = "APPR-$id",
            approvedAt = createdAt,
            status = PaymentStatus.APPROVED,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    private fun emptyPage() = PaymentPage(
        items = emptyList(),
        hasNext = false,
        nextCursorCreatedAt = null,
        nextCursorId = null,
    )

    private fun emptySummary() = PaymentSummaryProjection(
        count = 0,
        totalAmount = BigDecimal.ZERO,
        totalNetAmount = BigDecimal.ZERO,
    )
}
