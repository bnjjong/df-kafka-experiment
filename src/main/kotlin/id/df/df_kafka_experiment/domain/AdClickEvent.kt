package id.df.df_kafka_experiment.domain

import id.df.df_kafka_experiment.serialization.InstantIso8601Serializer
import java.time.Instant
import kotlinx.serialization.Serializable

// 광고 클릭 정보를 Kafka로 전송하기 위한 최소 필드 묶음
@Serializable // Kotlinx serializer가 스키마를 생성해 커스텀 직렬화 구현에서 활용
data class AdClickEvent(
    val audienceId: String,    // 광고를 클릭한 사용자를 식별하는 ID
    val inventoryId: String,   // 노출된 광고 지면(슬롯) ID
    val creativeId: String,    // 실제로 노출된 소재 ID
    val templateId: String,    // 렌더링 템플릿 ID
    val currentUrl: String,    // 클릭이 발생한 페이지 URL
    @Serializable(with = InstantIso8601Serializer::class) // Instant 기본 직렬화 지원이 없어 ISO 포맷으로 변환
    val clickedAt: Instant = Instant.now() // 이벤트 발생 시각(프로듀서에서 채움)
)
