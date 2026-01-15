package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import java.math.BigDecimal

data class CreatePaymentRequest(
    @field:Schema(description = "제휴사 식별자", example = "1")
    val partnerId: Long,
    @field:Schema(description = "결제 금액", example = "10000", minimum = "1")
    @field:Min(1)
    val amount: BigDecimal,
    @field:Schema(description = "카드 BIN", example = "123456")
    val cardBin: String? = null,
    @field:Schema(description = "카드 마지막 4자리", example = "4242")
    val cardLast4: String? = null,
    @field:Schema(description = "상품명", example = "샘플")
    val productName: String? = null,
)
