package im.bigs.pg.external.pg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.application.pg.port.out.TestPgResponse
import im.bigs.pg.domain.payment.PaymentStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
@Profile("test-pg")
class TestPgClient(
    private val webClient: WebClient,
    private val pgCrypto: PgCrypto,
    @Value("\${pg.test.api-key}")
    private val apiKey: String,
    @Value("\${pg.test.iv}")
    private val iv: String,
) : PgClientOutPort {

    override fun supports(partnerId: Long): Boolean = true

    override fun approve(request: PgApproveRequest): PgApproveResult {
        // 1. PG 문서 기준 평문 JSON 생성
        val plainJson = """
            {
              "cardNumber": "1111-1111-1111-1111",
              "birthDate": "19900101",
              "expiry": "1227",
              "password": "12",
              "amount": ${request.amount}
            }
        """.trimIndent()

        // 2. AES-256-GCM 암호화
        val enc = pgCrypto.encrypt(
            apiKey = apiKey,
            ivBase64Url = iv,
            plainJson = plainJson
        )

        // 3. Test PG 서버 호출
        val response = webClient.post()
            .uri("/api/v1/pay/credit-card")
            .header("API-KEY", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("enc" to enc))
            .retrieve()
            .bodyToMono(TestPgResponse::class.java)
            .block()!!

        // 4. 결과 매핑
        return PgApproveResult(
            approvalCode = response.approvalCode,
            approvedAt = response.approvedAt,
            status = PaymentStatus.APPROVED,
        )
    }
}
