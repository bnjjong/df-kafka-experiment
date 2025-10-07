package id.df.df_kafka_experiment.producer

import id.df.df_kafka_experiment.config.AdClickLoadTestProperties
import id.df.df_kafka_experiment.domain.AdClickEvent
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

// 애플리케이션 기동 시 자동으로 100,000건 등 대량 전송 실험을 수행하는 러너
@Component
class AdClickLoadTestRunner(
    private val adClickProducer: AdClickProducer,
    private val loadTestProperties: AdClickLoadTestProperties
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (!loadTestProperties.enabled) {
            logger.info("AdClick load test disabled - skip execution")
            return
        }

        val totalEvents = loadTestProperties.totalEvents
        if (totalEvents <= 0) {
            logger.warn("Load test requested but totalEvents={} is not positive - skip", totalEvents)
            return
        }

        val scenario = loadTestProperties.scenarioLabel.ifEmpty { "default" }
        val startTime = Instant.now()
        logger.info(
            "Starting ad click load test: scenario={}, totalEvents={}",
            scenario,
            totalEvents
        )

        val pendingResults = mutableListOf<CompletableFuture<*>>()
        repeat(totalEvents) { index ->
            val event = AdClickEvent(
                audienceId = "aud-${index % 10_000}",
                inventoryId = "inv-${index % 1_000}",
                creativeId = "crt-${index % 500}",
                templateId = "tmpl-${index % 100}",
                currentUrl = "https://example.com/landing?seq=$index"
            )

            // Future를 모아 두면 Kafka 프로듀서가 배치를 채우며 전송할 수 있어 linger/batch 설정이 효과를 낼 수 있다
            pendingResults += adClickProducer.send(event)
        }

        pendingResults.forEachIndexed { idx, future ->
            try {
                // join()으로 모든 전송이 끝났는지 확인해 실패를 조기에 감지
                // 순서대로 돌면서 처리 특정 index 지연이 발생할 경우 그 뒤에 future들은 완료가 되어도 대기 해야 함.
                future.join()
            } catch (ex: CompletionException) {
                logger.error(
                    "Ad click load test failed while awaiting send result: scenario={}, index={}",
                    scenario,
                    idx,
                    ex.cause ?: ex
                )
                throw ex.cause ?: ex
            } catch (ex: Exception) {
                logger.error(
                    "Ad click load test failed while awaiting send result: scenario={}, index={}",
                    scenario,
                    idx,
                    ex
                )
                throw ex
            }
        }

        val duration = Duration.between(startTime, Instant.now())
        logger.info(
            "Completed ad click load test: scenario={}, totalEvents={}, duration={} ms",
            scenario,
            totalEvents,
            duration.toMillis()
        )

        // 누적된 직렬화/전송 통계를 즉시 출력해 벤치 결과를 확인할 수 있게 함
        adClickProducer.logSummary(trigger = "load-test:$scenario")
    }
}
