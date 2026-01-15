package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.`in`.QueryPaymentsUseCase
import im.bigs.pg.application.payment.port.`in`.QueryResult
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.domain.payment.PaymentSummary
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64

/**
 * 결제 이력 조회 유스케이스 구현체.
 * - 커서 기반 페이지네이션(createdAt desc, id desc)
 * - 통계는 조회 조건과 동일한 전체 집합 기준으로 계산
 */
@Service
class QueryPaymentsService(
    private val paymentOutPort: PaymentOutPort,
) : QueryPaymentsUseCase {

    override fun query(filter: QueryFilter): QueryResult {
        /* 1. 커서 복원 */
        val (cursorAt, cursorId) = decodeCursor(filter.cursor)

        /* 2. 페이지 조회 */
        val page = paymentOutPort.findBy(
            PaymentQuery(
                partnerId = filter.partnerId,
                status = PaymentStatus.from((filter.status)),
                from = filter.from,
                to = filter.to,
                limit = filter.limit,
                cursorCreatedAt = cursorAt?.let {
                    LocalDateTime.ofInstant(it, ZoneOffset.UTC)
                },
                cursorId = cursorId,
            )
        )

        /* 3. 통계 조회 (cursor 제외, 동일 필터 집합) */
        val summaryProjection = paymentOutPort.summary(
            PaymentSummaryFilter(
                partnerId = filter.partnerId,
                status = PaymentStatus.from((filter.status)),
                from = filter.from,
                to = filter.to,
            )
        )

        /* 4. 다음 커서 생성 */
        val nextCursor = if (page.hasNext) {
            encodeCursor(
                page.nextCursorCreatedAt?.toInstant(ZoneOffset.UTC),
                page.nextCursorId,
            )
        } else {
            null
        }

        /* 5. 결과 조합 */
        return QueryResult(
            items = page.items,
            summary = PaymentSummary(
                count = summaryProjection.count,
                totalAmount = summaryProjection.totalAmount,
                totalNetAmount = summaryProjection.totalNetAmount,
            ),
            nextCursor = nextCursor,
            hasNext = page.hasNext,
        )
    }

    /** 다음 페이지 이동을 위한 커서 인코딩. */
    private fun encodeCursor(createdAt: Instant?, id: Long?): String? {
        if (createdAt == null || id == null) return null
        val raw = "${createdAt.toEpochMilli()}:$id"
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(raw.toByteArray())
    }

    /** 요청으로 전달된 커서 복원. 유효하지 않으면 null 커서로 간주 */
    private fun decodeCursor(cursor: String?): Pair<Instant?, Long?> {
        if (cursor.isNullOrBlank()) return null to null
        return try {
            val raw = String(Base64.getUrlDecoder().decode(cursor))
            val parts = raw.split(":")
            Instant.ofEpochMilli(parts[0].toLong()) to parts[1].toLong()
        } catch (e: Exception) {
            null to null
        }
    }
}
