package id.df.df_kafka_experiment

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DfKafkaExperimentApplicationTest : DescribeSpec({
    describe("Application context") {
        it("loads") {}
    }
}) {
    init {
        extension(SpringExtension)
    }
}
