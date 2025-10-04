package id.df.df_kafka_experiment.api

import id.df.df_kafka_experiment.producer.AdClickProducer
import id.df.df_kafka_experiment.api.dto.AdClickRequest
import id.df.df_kafka_experiment.api.dto.AdClickResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

// REST API를 통해 광고 클릭 이벤트를 Kafka로 전달하는 컨트롤러
@RestController
@RequestMapping("/api/ad-clicks")
class AdClickController(
    private val adClickProducer: AdClickProducer
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun publish(@RequestBody request: AdClickRequest): ResponseEntity<AdClickResponse> {
        val event = request.toEvent() // 요청 본문을 도메인 이벤트로 변환
        val future = adClickProducer.send(event) // Kafka에 비동기 전송 요청

        // 학습용 샘플이라 전송 결과를 기다렸다가 응답에 포함시킴
        val sendResult = future.get(5, TimeUnit.SECONDS)
        val metadata = sendResult.recordMetadata

        logger.info("Successfully sent ad click event to topic={}, partition={}, offset={}", metadata.topic(), metadata.partition(), metadata.offset())

        return ResponseEntity.accepted().body(AdClickResponse.Companion.from(metadata))
    }
}