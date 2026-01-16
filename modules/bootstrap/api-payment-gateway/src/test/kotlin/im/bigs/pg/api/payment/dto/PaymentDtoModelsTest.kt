package im.bigs.pg.api.payment.dto

import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaymentDtoModelsTest {

    @Test
    @DisplayName("CreatePaymentRequest 기본값이 설정된다")
    fun `CreatePaymentRequest의 기본값 설정 검증`() {
        val request = CreatePaymentRequest(
            partnerId = 1L,
            amount = BigDecimal("1000"),
        )

        assertEquals(1L, request.partnerId)
        assertEquals(BigDecimal("1000"), request.amount)
        assertNull(request.cardBin)
        assertNull(request.cardLast4)
        assertNull(request.productName)
    }

    @Test
    @DisplayName("Payment 도메인을 응답 DTO로 변환한다")
    fun `Payment 도메인을 ResponseDTO로 변환하는 과정 검증`() {
        val now = LocalDateTime.of(2024, 1, 1, 0, 0)
        val payment = Payment(
            id = 99L,
            partnerId = 1L,
            amount = BigDecimal("10000"),
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = BigDecimal("400"),
            netAmount = BigDecimal("9600"),
            cardBin = "123456",
            cardLast4 = "4242",
            approvalCode = "APPROVAL",
            approvedAt = now,
            status = PaymentStatus.APPROVED,
            createdAt = now,
            updatedAt = now,
        )

        val response = PaymentResponse.from(payment)

        assertEquals(payment.id, response.id)
        assertEquals(payment.amount, response.amount)
        assertEquals(payment.netAmount, response.netAmount)
        assertEquals(payment.status, response.status)
    }
}
