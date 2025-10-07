package id.df.df_kafka_experiment.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import id.df.df_kafka_experiment.domain.AdClickEvent
import org.apache.kafka.common.serialization.Serializer

class JacksonAdClickEventSerializer(
    private val objectMapper: ObjectMapper
) : Serializer<AdClickEvent> {

    override fun serialize(topic: String?, data: AdClickEvent?): ByteArray? {
        if (data == null) return null
        return objectMapper.writeValueAsBytes(data)
    }
}
