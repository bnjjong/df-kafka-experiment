package id.df.df_kafka_experiment.config

import jakarta.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

// 대량 전송 실험을 제어하는 옵션 묶음
@Validated
@ConfigurationProperties(prefix = "ad-clicks.load-test")
class AdClickLoadTestProperties {
    var enabled: Boolean = false        // true일 때 애플리케이션 기동 시 대량 전송 실행

    @field:PositiveOrZero
    var totalEvents: Int = 0            // 전송할 총 이벤트 수 (예: 100_000)

    var scenarioLabel: String = ""      // 실행 시 로그에 남길 시나리오 이름
}
