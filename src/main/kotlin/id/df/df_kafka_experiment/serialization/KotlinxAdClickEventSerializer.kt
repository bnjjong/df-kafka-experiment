package id.df.df_kafka_experiment.serialization

import id.df.df_kafka_experiment.domain.AdClickEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Serializer
import kotlin.text.Charsets

class KotlinxAdClickEventSerializer(
    private val json: Json
) : Serializer<AdClickEvent> {

    private val delegate = AdClickEvent.serializer()

    override fun serialize(topic: String?, data: AdClickEvent?): ByteArray? {
        if (data == null) return null
        val payload = json.encodeToString(delegate, data)
        return payload.toByteArray(Charsets.UTF_8)
    }
}
