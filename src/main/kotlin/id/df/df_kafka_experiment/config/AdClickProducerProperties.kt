package id.df.df_kafka_experiment.config

import jakarta.annotation.PostConstruct
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

// Kafka 프로듀서 관련 필수 설정을 바인딩하는 Properties 클래스
@Validated
@ConfigurationProperties(prefix = "ad-clicks.producer")
class AdClickProducerProperties {

    @field:NotBlank
    lateinit var topic: String       // 전송 대상 토픽명

    @field:NotBlank
    lateinit var acks: String        // 브로커 확인 방식(신뢰도와 지연에 영향)

    @field:PositiveOrZero
    var lingerMs: Long = 0           // 메시지를 모아 보내는 대기 시간(ms)

    @field:Positive
    var batchSize: Int = 1           // 배치 한 번에 모을 최대 바이트 수

    @field:NotBlank
    lateinit var compressionType: String // 전송 시 사용할 압축 타입

    @field:NotNull
    var enableIdempotence: Boolean? = null // 중복 전송 방지 플래그

    val enableIdempotenceRequired: Boolean
        get() = requireNotNull(enableIdempotence) { "ad-clicks.producer.enable-idempotence 값이 설정되어야 합니다." }

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun validateAndLog() {
        require(::topic.isInitialized) { "ad-clicks.producer.topic 값이 설정되어야 합니다." }
        require(::acks.isInitialized) { "ad-clicks.producer.acks 값이 설정되어야 합니다." }
        require(::compressionType.isInitialized) { "ad-clicks.producer.compression-type 값이 설정되어야 합니다." }
        requireNotNull(enableIdempotence) { "ad-clicks.producer.enable-idempotence 값이 설정되어야 합니다." }
        require(batchSize > 0) { "ad-clicks.producer.batch-size 는 0보다 커야 합니다." }

        logger.info(
            "AdClickProducerProperties bound successfully - topic={}, acks={}, lingerMs={}, batchSize={}, compressionType={}, enableIdempotence={}",
            topic,
            acks,
            lingerMs,
            batchSize,
            compressionType,
            enableIdempotence
        )
    }
}
