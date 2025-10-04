package id.df.df_kafka_experiment.config

import id.df.df_kafka_experiment.domain.AdClickEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableConfigurationProperties(AdClickProducerProperties::class)
class KafkaProducerConfig {

    @Bean
    fun adClickProducerFactory(
        kafkaProperties: KafkaProperties,
        producerProperties: AdClickProducerProperties
    ): ProducerFactory<String, AdClickEvent> {
        // Spring Boot가 관리하는 기본 producer 설정을 불러온 뒤 커스텀 옵션 덮어쓰기
        val props = kafkaProperties.buildProducerProperties().toMutableMap()
        props[ProducerConfig.ACKS_CONFIG] = producerProperties.acks
        props[ProducerConfig.LINGER_MS_CONFIG] = producerProperties.lingerMs
        props[ProducerConfig.BATCH_SIZE_CONFIG] = producerProperties.batchSize
        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = producerProperties.compressionType
        props[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = producerProperties.enableIdempotence

        // JsonSerializer는 데이터 클래스를 JSON으로 변환하여 전송
        val valueSerializer = JsonSerializer<AdClickEvent>().apply {
            setAddTypeInfo(false) // 헤더에 타입 정보를 넣지 않아 컨슈머가 단순 JSON으로 읽을 수 있게 구성
        }

        return DefaultKafkaProducerFactory(
            props,
            StringSerializer(),
            valueSerializer
        )
    }

    @Bean
    fun adClickKafkaTemplate(
        producerFactory: ProducerFactory<String, AdClickEvent>
    ): KafkaTemplate<String, AdClickEvent> = KafkaTemplate(producerFactory)
        // KafkaTemplate은 send() 호출 시 CompletableFuture를 반환해 비동기 결과 확인 가능
}
