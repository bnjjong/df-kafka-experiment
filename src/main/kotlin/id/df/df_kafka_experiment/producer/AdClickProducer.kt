package id.df.df_kafka_experiment.producer

import id.df.df_kafka_experiment.config.AdClickProducerProperties
import id.df.df_kafka_experiment.domain.AdClickEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class AdClickProducer(
    private val kafkaTemplate: KafkaTemplate<String, AdClickEvent>,
    private val properties: AdClickProducerProperties
) {
    fun send(event: AdClickEvent): CompletableFuture<SendResult<String, AdClickEvent>> {
        val key = event.audienceId // 같은 사용자 이벤트를 한 파티션으로 모으기 위한 메시지 키
        return kafkaTemplate.send(properties.topic, key, event) // 비동기로 전송하고 결과 future 반환
    }
}
