package id.df.df_kafka_experiment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "ad-clicks.producer")
data class AdClickProducerProperties(
    val topic: String,        // 전송 대상 토픽명 (필수 값, 없으면 바인딩 실패)
    val acks: String,         // 브로커 확인 방식(신뢰도와 지연에 영향)
    val lingerMs: Long,       // 메시지를 모아 보내는 대기 시간(ms)
    val batchSize: Int,       // 배치 한 번에 모을 최대 바이트 수
    val compressionType: String, // 전송 시 사용할 압축 타입
    val enableIdempotence: Boolean // 중복 전송 방지를 위한 멱등성 플래그
)
