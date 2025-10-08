package id.df.df_kafka_experiment.config

import id.df.df_kafka_experiment.domain.AdClickEvent
import id.df.df_kafka_experiment.serialization.ByteBufferAdClickEventSerializer
import id.df.df_kafka_experiment.serialization.DataOutputStreamAdClickEventSerializer
import id.df.df_kafka_experiment.serialization.JacksonAdClickEventSerializer
import id.df.df_kafka_experiment.serialization.KotlinxAdClickEventSerializer
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper

@Configuration
@EnableConfigurationProperties(value = [AdClickProducerProperties::class, AdClickLoadTestProperties::class])
class KafkaProducerConfig {

    @Bean
    fun adClickProducerFactory(
        kafkaProperties: KafkaProperties,
        producerProperties: AdClickProducerProperties,
        @Qualifier("adClickValueSerializer") valueSerializer: Serializer<AdClickEvent>
    ): ProducerFactory<String, AdClickEvent> {
        // Spring Boot가 관리하는 기본 producer 설정을 불러온 뒤 커스텀 옵션 덮어쓰기
        val props = kafkaProperties.buildProducerProperties().toMutableMap()
        props[ProducerConfig.ACKS_CONFIG] = producerProperties.acks
        props[ProducerConfig.LINGER_MS_CONFIG] = producerProperties.lingerMs
        props[ProducerConfig.BATCH_SIZE_CONFIG] = producerProperties.batchSize
        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = producerProperties.compressionType
        props[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = producerProperties.enableIdempotenceRequired
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

    // 프로듀서 직렬화 전략을 profile/property로 쉽게 바꿀 수 있도록 Bean으로 분리
    @Bean(name = ["adClickValueSerializer"])
    fun adClickValueSerializer(
        producerProperties: AdClickProducerProperties,
        objectMapper: ObjectMapper,
        kotlinxJson: Json
    ): Serializer<AdClickEvent> = when (producerProperties.serializer) {
        ProducerSerializerType.SPRING_JSON -> JsonSerializer<AdClickEvent>().apply {
            setAddTypeInfo(false) // 헤더에 타입 정보를 넣지 않아 컨슈머가 단순 JSON으로 읽을 수 있게 구성
        }

        ProducerSerializerType.JACKSON_OBJECT_MAPPER -> JacksonAdClickEventSerializer(objectMapper.copy())

        ProducerSerializerType.KOTLINX_JSON -> KotlinxAdClickEventSerializer(kotlinxJson)

        ProducerSerializerType.BYTE_BUFFER -> ByteBufferAdClickEventSerializer()

        ProducerSerializerType.DATA_OUTPUT_STREAM -> DataOutputStreamAdClickEventSerializer()
    }

    // kotlinx.serialization 기반 구현에서 공통 옵션을 재사용하기 위한 Json 인스턴스
    @Bean
    fun kotlinxJson(): Json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = false
    }
}
