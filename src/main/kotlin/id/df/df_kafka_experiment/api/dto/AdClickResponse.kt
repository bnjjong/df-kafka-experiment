package id.df.df_kafka_experiment.api.dto

import org.apache.kafka.clients.producer.RecordMetadata

// Kafka 전송 결과 정보를 API 응답으로 노출하기 위한 DTO
data class AdClickResponse(
    val topic: String,
    val partition: Int,
    val offset: Long,
    val timestamp: Long
) {
    companion object {
        fun from(metadata: RecordMetadata): AdClickResponse = AdClickResponse(
            topic = metadata.topic(),
            partition = metadata.partition(),
            offset = metadata.offset(),
            timestamp = metadata.timestamp()
        )
    }
}
