package id.df.df_kafka_experiment.serialization

import id.df.df_kafka_experiment.domain.AdClickEvent
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import org.apache.kafka.common.serialization.Serializer
import kotlin.text.Charsets

/**
 * Serializes AdClickEvent using DataOutputStream for straightforward binary encoding.
 */
class DataOutputStreamAdClickEventSerializer : Serializer<AdClickEvent> {

    override fun serialize(topic: String?, data: AdClickEvent?): ByteArray? {
        if (data == null) return null

        val buffer = ByteArrayOutputStream()
        DataOutputStream(buffer).use { out ->
            fun writeWithLength(value: String) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                out.writeInt(bytes.size)
                out.write(bytes)
            }

            writeWithLength(data.audienceId)
            writeWithLength(data.inventoryId)
            writeWithLength(data.creativeId)
            writeWithLength(data.templateId)
            writeWithLength(data.currentUrl)
            out.writeLong(data.clickedAt.toEpochMilli())
        }

        return buffer.toByteArray()
    }
}
