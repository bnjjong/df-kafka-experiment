package id.df.df_kafka_experiment.domain

import java.time.Instant

// 광고 클릭 정보를 Kafka로 전송하기 위한 최소 필드 묶음
data class AdClickEvent(
    val audienceId: String,    // 광고를 클릭한 사용자를 식별하는 ID
    val inventoryId: String,   // 노출된 광고 지면(슬롯) ID
    val creativeId: String,    // 실제로 노출된 소재 ID
    val templateId: String,    // 렌더링 템플릿 ID
    val currentUrl: String,    // 클릭이 발생한 페이지 URL
    val clickedAt: Instant = Instant.now() // 이벤트 발생 시각(프로듀서에서 채움)
)
