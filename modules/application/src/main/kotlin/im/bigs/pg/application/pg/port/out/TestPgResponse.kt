package im.bigs.pg.application.pg.port.out

import java.time.LocalDateTime

data class TestPgResponse(
    val approvalCode: String,
    val approvedAt: LocalDateTime,
    val maskedCardLast4: String,
    val amount: Long,
    val status: String,
)
