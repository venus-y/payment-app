package im.bigs.pg.application.payment.service

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.application.partner.port.out.PartnerOutPort
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.`in`.PaymentUseCase
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.calculation.FeeCalculator
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import org.springframework.stereotype.Service

/**
 * 결제 생성 유스케이스 구현체.
 * - 입력(REST 등) → 도메인/외부PG/영속성 포트를 순차적으로 호출하는 흐름을 담당합니다.
 * - 수수료 정책 조회 및 적용(계산)은 도메인 유틸리티를 통해 수행합니다.
 */
@Service
class PaymentService(
    private val partnerRepository: PartnerOutPort,
    private val feePolicyRepository: FeePolicyOutPort,
    private val paymentRepository: PaymentOutPort,
    private val pgClients: List<PgClientOutPort>,
) : PaymentUseCase {

    override fun pay(command: PaymentCommand): Payment {
        val partner = partnerRepository.findById(command.partnerId)
            ?: throw IllegalArgumentException("Partner not found: ${command.partnerId}")
        require(partner.active) { "Partner is inactive: ${partner.id}" }

        val pgClient = pgClients.firstOrNull { it.supports(partner.id) }
            ?: throw IllegalStateException("No PG client for partner ${partner.id}")

        val approve = pgClient.approve(
            PgApproveRequest(
                partnerId = partner.id,
                amount = command.amount,
                cardBin = command.cardBin,
                cardLast4 = command.cardLast4,
                productName = command.productName,
            ),
        )

        // 1. 승인 시점 기준 정책 조회
        val policy = feePolicyRepository.findEffectivePolicy(
            partnerId = partner.id,
            at = approve.approvedAt
        ) ?: throw IllegalStateException("Fee policy not found for partner ${partner.id}")

        // 2. 정책 기반 수수료 계산
        val (fee, net) = FeeCalculator.calculateFee(
            amount = command.amount,
            rate = policy.percentage,
            fixed = policy.fixedFee
        )

        // 3. 정책 기반 결제 엔티티 생성
        val payment = Payment(
            partnerId = partner.id,
            amount = command.amount,
            appliedFeeRate = policy.percentage,
            feeAmount = fee,
            netAmount = net,
            cardBin = command.cardBin,
            cardLast4 = command.cardLast4,
            approvalCode = approve.approvalCode,
            approvedAt = approve.approvedAt,
            status = PaymentStatus.APPROVED,
        )

        return paymentRepository.save(payment)
    }
}


