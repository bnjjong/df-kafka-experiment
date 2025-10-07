package id.df.df_kafka_experiment.serialization

import java.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes [Instant] values as ISO-8601 strings for kotlinx.serialization.
 */
object InstantIso8601Serializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InstantIso8601", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}
