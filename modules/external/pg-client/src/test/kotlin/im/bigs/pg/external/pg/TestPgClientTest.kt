package im.bigs.pg.external.pg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.TestPgResponse
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime



@ExtendWith(MockKExtension::class)
class TestPgClientTest {

    @MockK
    lateinit var webClient: WebClient

    @MockK
    lateinit var pgCrypto: PgCrypto

    @MockK
    lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec

    @MockK
    lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>

    @MockK
    lateinit var responseSpec: WebClient.ResponseSpec

    private lateinit var testPgClient: TestPgClient

    @BeforeEach
    fun setUp() {
        testPgClient = TestPgClient(
            webClient = webClient,
            pgCrypto = pgCrypto,
            apiKey = "test-api-key",
            iv = "test-iv"
        )
    }

    @Test
    fun `approve - 정상 응답 매핑`() {
        // given
        val request = PgApproveRequest(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            cardBin = null,
            cardLast4 = null,
            productName = null
        )

        every {
            pgCrypto.encrypt(any(), any(), any())
        } returns "encrypted-value"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(any(), any<String>()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.contentType(any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec

        every {
            responseSpec.bodyToMono(TestPgResponse::class.java)
        } returns Mono.just(
            TestPgResponse(
                approvalCode = "12345678",
                approvedAt = LocalDateTime.now(),
                maskedCardLast4 = "1111",
                amount = 10000L,
                status = "APPROVED"
            )
        )

        // when
        val result = testPgClient.approve(request)

        // then
        assertEquals("12345678", result.approvalCode)
        assertEquals(PaymentStatus.APPROVED, result.status)

        verify(exactly = 1) {
            pgCrypto.encrypt(
                apiKey = "test-api-key",
                ivBase64Url = "test-iv",
                plainJson = match { it.contains("10000") }
            )
        }
    }
}
