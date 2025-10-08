package id.df.df_kafka_experiment.config

/**
 * Available serializer implementations for publishing AdClick events.
 */
enum class ProducerSerializerType {
    SPRING_JSON,
    JACKSON_OBJECT_MAPPER,
    KOTLINX_JSON,
    BYTE_BUFFER,
    DATA_OUTPUT_STREAM
}
