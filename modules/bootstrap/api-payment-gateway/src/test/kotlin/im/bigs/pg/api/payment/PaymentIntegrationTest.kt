package im.bigs.pg.api.payment

import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.`in`.PaymentUseCase
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class PaymentIntegrationTest {

    @Autowired
    lateinit var paymentUseCase: PaymentUseCase
    @Autowired lateinit var paymentJpaRepository: PaymentJpaRepository

    @Test
    fun `결제 생성 시 DB에 정책 기반 금액이 저장된다`() {
        // given
        val command = PaymentCommand(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            cardLast4 = "4242",
            productName = "통합테스트 상품"
        )

        // when
        val payment = paymentUseCase.pay(command)

        // then
        val saved = paymentJpaRepository.findById(payment.id!!)
            .orElseThrow()

        assertEquals(BigDecimal("10000"), saved.amount)
        assertEquals(BigDecimal("0.0235"), saved.appliedFeeRate)
        assertEquals(BigDecimal("235"), saved.feeAmount)
        assertEquals(BigDecimal("9765"), saved.netAmount)
    }
}
