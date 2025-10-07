package id.df.df_kafka_experiment.producer

import id.df.df_kafka_experiment.config.AdClickProducerProperties
import id.df.df_kafka_experiment.domain.AdClickEvent
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.DoubleAdder
import java.util.concurrent.atomic.LongAdder

@Service
class AdClickProducer(
    private val kafkaTemplate: KafkaTemplate<String, AdClickEvent>,
    private val properties: AdClickProducerProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val successCount = LongAdder() // 성공 메시지 수를 누적
    private val failureCount = LongAdder() // 실패 메시지 수를 누적
    private val totalSizeBytes = LongAdder() // 성공 메시지의 직렬화된 총 바이트 합계
    private val totalDurationMillis = DoubleAdder() // 성공 메시지 전송 소요 시간 합계

    fun send(event: AdClickEvent): CompletableFuture<SendResult<String, AdClickEvent>> {
        val key = event.audienceId // 같은 사용자 이벤트를 한 파티션으로 모으기 위한 메시지 키
        val startNanos = System.nanoTime() // 전송 시작 시각(나노초) - 지속시간 계산용
        return kafkaTemplate.send(properties.topic, key, event) // 비동기로 전송하고 결과 future 반환
            .whenComplete { result, error ->
                val durationMillis = (System.nanoTime() - startNanos) / 1_000_000.0

                if (error != null) {
                    failureCount.increment()
                    logger.error(
                        "Failed to send AdClickEvent key={} durationMillis={} serializer={}",
                        key,
                        String.format(Locale.US, "%.3f", durationMillis),
                        properties.serializer,
                        error
                    )
                    return@whenComplete
                }

                if (result != null) {
                    successCount.increment()
                    val messageSize = result.recordMetadata.serializedValueSize() // 직렬화된 value 바이트 크기
                    totalSizeBytes.add(messageSize.toLong())
                    totalDurationMillis.add(durationMillis)
                }
            }
    }

    // 누적된 통계를 로그로 남기고 필요 시 다시 0으로 초기화
    fun logSummary(trigger: String, resetCounters: Boolean = true) {
        val successes = if (resetCounters) successCount.sumThenReset() else successCount.sum()
        val failures = if (resetCounters) failureCount.sumThenReset() else failureCount.sum()

        if (successes == 0L && failures == 0L) {
            logger.info(
                "AdClickProducer summary serializer={} trigger={} - no messages produced",
                properties.serializer,
                trigger
            )
            return
        }

        val totalBytes = if (resetCounters) totalSizeBytes.sumThenReset() else totalSizeBytes.sum()
        val totalDuration = if (resetCounters) totalDurationMillis.sumThenReset() else totalDurationMillis.sum()
        val avgSize = if (successes > 0L) totalBytes.toDouble() / successes else 0.0
        val avgDuration = if (successes > 0L) totalDuration / successes else 0.0

        logger.info(
            "AdClickProducer summary serializer={} trigger={} success={} failure={} avgSizeBytes={} avgDurationMillis={} totalSizeBytes={} totalDurationMillis={}",
            properties.serializer,
            trigger,
            successes,
            failures,
            String.format(Locale.US, "%.2f", avgSize),
            String.format(Locale.US, "%.3f", avgDuration),
            totalBytes,
            String.format(Locale.US, "%.3f", totalDuration)
        )
    }

    @PreDestroy
    fun logSummaryAtShutdown() {
        logSummary(trigger = "shutdown", resetCounters = true)
    }
}
