package im.bigs.pg.domain.payment

import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaymentStatusTest {

    @Test
    @DisplayName("결제 상태 문자열은 대소문자/공백을 무시하고 매핑된다")
    fun `상태 문자열 매핑 성공`() {
        assertEquals(PaymentStatus.APPROVED, PaymentStatus.from("approved"))
        assertEquals(PaymentStatus.CANCELED, PaymentStatus.from(" CANCELED "))
    }

    @Test
    @DisplayName("결제 상태 문자열이 비어 있거나 알 수 없으면 null을 반환한다")
    fun `상태 문자열 매핑 실패`() {
        assertNull(PaymentStatus.from(null))
        assertNull(PaymentStatus.from(""))
        assertNull(PaymentStatus.from("  "))
        assertNull(PaymentStatus.from("unknown"))
    }
}
