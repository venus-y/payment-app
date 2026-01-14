package im.bigs.pg.api.payment

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var paymentOutPort : PaymentOutPort

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
        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(command)
        }.andExpect {
            status { isOk() }
        }

        // then
        val page = paymentOutPort.findBy(PaymentQuery())
        val saved = page.items.first()

        assertEquals(BigDecimal("10000"), saved.amount)
        assertEquals(BigDecimal("0.0235"), saved.appliedFeeRate)
        assertEquals(BigDecimal("235"), saved.feeAmount)
        assertEquals(BigDecimal("9765"), saved.netAmount)
    }

}
