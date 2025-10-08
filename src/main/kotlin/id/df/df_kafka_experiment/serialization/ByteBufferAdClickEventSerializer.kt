package id.df.df_kafka_experiment.serialization

import id.df.df_kafka_experiment.domain.AdClickEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.apache.kafka.common.serialization.Serializer
import kotlin.text.Charsets

/**
 * Packs AdClickEvent fields into a single ByteBuffer.<br>
 * Layout: [len+bytes x5] + [clickedAt epoch milli as Long].
 */
class ByteBufferAdClickEventSerializer : Serializer<AdClickEvent> {

    override fun serialize(topic: String?, data: AdClickEvent?): ByteArray? {
        if (data == null) return null

        val audienceBytes = data.audienceId.toByteArray(Charsets.UTF_8)
        val inventoryBytes = data.inventoryId.toByteArray(Charsets.UTF_8)
        val creativeBytes = data.creativeId.toByteArray(Charsets.UTF_8)
        val templateBytes = data.templateId.toByteArray(Charsets.UTF_8)
        val urlBytes = data.currentUrl.toByteArray(Charsets.UTF_8)
        val clickedAtMillis = data.clickedAt.toEpochMilli()

        val totalSize = Int.SIZE_BYTES * 5 + // 각 문자열 길이 정보
            audienceBytes.size +
            inventoryBytes.size +
            creativeBytes.size +
            templateBytes.size +
            urlBytes.size +
            Long.SIZE_BYTES // Instant epoch milli 저장

        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.BIG_ENDIAN)

        fun putWithLength(bytes: ByteArray) {
            buffer.putInt(bytes.size)
            buffer.put(bytes)
        }

        putWithLength(audienceBytes)
        putWithLength(inventoryBytes)
        putWithLength(creativeBytes)
        putWithLength(templateBytes)
        putWithLength(urlBytes)
        buffer.putLong(clickedAtMillis)

        return buffer.array()
    }
}
