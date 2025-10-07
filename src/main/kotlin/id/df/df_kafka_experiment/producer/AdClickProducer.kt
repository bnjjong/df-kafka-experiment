package id.df.df_kafka_experiment.producer

import id.df.df_kafka_experiment.config.AdClickProducerProperties
import id.df.df_kafka_experiment.domain.AdClickEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.Locale

@Service
class AdClickProducer(
    private val kafkaTemplate: KafkaTemplate<String, AdClickEvent>,
    private val properties: AdClickProducerProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(event: AdClickEvent): CompletableFuture<SendResult<String, AdClickEvent>> {
        val key = event.audienceId // 같은 사용자 이벤트를 한 파티션으로 모으기 위한 메시지 키
        val startNanos = System.nanoTime() // 전송 시작 시각(나노초) - 지속시간 계산용
        return kafkaTemplate.send(properties.topic, key, event) // 비동기로 전송하고 결과 future 반환
            .whenComplete { result, error ->
                val durationMillis = (System.nanoTime() - startNanos) / 1_000_000.0
                val formattedDuration = String.format(Locale.US, "%.3f", durationMillis)

                if (error != null) {
                    logger.error(
                        "Failed to send AdClickEvent key={} durationMillis={} serializer={}",
                        key,
                        formattedDuration,
                        properties.serializer,
                        error
                    )
                    return@whenComplete
                }

                if (result != null) {
                    val messageSize = result.recordMetadata.serializedValueSize() // 직렬화된 value 바이트 크기
                    logger.info(
                        "Produced AdClickEvent key={} sizeBytes={} durationMillis={} serializer={}",
                        key,
                        messageSize,
                        formattedDuration,
                        properties.serializer
                    )
                }
            }
    }
}
