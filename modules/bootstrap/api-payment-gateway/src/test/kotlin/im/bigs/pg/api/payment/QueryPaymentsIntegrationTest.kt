package im.bigs.pg.api.payment

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QueryPaymentsIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        createPayment(1, 10_000)
        createPayment(1, 20_000)
        createPayment(2, 30_000)
    }

    @Test
    fun `limit과 무관하게 summary는 전체 집합 기준이다`() {
        mockMvc.get("/api/v1/payments") {
            param("limit", "1")
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.summary.count") { value(3) }
            jsonPath("$.hasNext") { value(true) }
            jsonPath("$.nextCursor") { isNotEmpty() }
        }
    }

    @Test
    fun `partnerId 필터 적용 시 summary도 동일 집합 기준이다`() {
        mockMvc.get("/api/v1/payments") {
            param("partnerId", "1")
        }.andExpect {
            status { isOk() }
            jsonPath("$.summary.count") { value(2) }
        }
    }

    @Test
    fun `잘못된 status 값은 무시되고 전체 조회된다`() {
        mockMvc.get("/api/v1/payments") {
            param("status", "wrongValue")
        }.andExpect {
            status { isOk() }
            jsonPath("$.summary.count") { value(3) }
        }
    }

    @Test
    fun `커서 기반 페이지네이션이 정상 동작한다`() {
        val firstJson = mockMvc.get("/api/v1/payments") {
            param("limit", "1")
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.hasNext") { value(true) }
            jsonPath("$.nextCursor") { isNotEmpty() }
        }.andReturn().response.contentAsString

        val cursor = JsonPath.read<String>(firstJson, "$.nextCursor")

        mockMvc.get("/api/v1/payments") {
            param("limit", "1")
            param("cursor", cursor)
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
        }
    }

    @Test
    fun `잘못된 커서는 무시되고 첫 페이지가 반환된다`() {
        mockMvc.get("/api/v1/payments") {
            param("cursor", "invalidCursor")
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(3) }
            jsonPath("$.summary.count") { value(3) }
        }
    }

    private fun createPayment(partnerId: Long, amount: Int) {
        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "partnerId": $partnerId,
                  "amount": $amount,
                  "productName": "TEST"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }
    }
}
