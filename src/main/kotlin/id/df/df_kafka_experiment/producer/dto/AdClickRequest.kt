package id.df.df_kafka_experiment.producer.dto

import id.df.df_kafka_experiment.domain.AdClickEvent

// API 요청 본문을 도메인 이벤트로 바꾸기 위한 DTO
data class AdClickRequest(
    val audienceId: String,
    val inventoryId: String,
    val creativeId: String,
    val templateId: String,
    val currentUrl: String
) {
    fun toEvent(): AdClickEvent = AdClickEvent(
        audienceId = audienceId,
        inventoryId = inventoryId,
        creativeId = creativeId,
        templateId = templateId,
        currentUrl = currentUrl
    )
}
