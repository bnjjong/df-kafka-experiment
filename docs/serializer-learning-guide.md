# Serializer 학습 가이드

Kafka 프로듀서에서 다양한 Serializer 조합을 실험하고 퍼포먼스 특성을 이해하기 위한 실습 아이디어를 정리했습니다. 각 항목은 직접 측정 가능한 기준을 포함하므로 결과를 수치화하여 비교해 보세요.

## 1. 단일 전송 vs 배치 전송
- `linger.ms`, `batch.size` 값을 서로 다르게 설정하고 동일한 이벤트를 1,000회 연속 전송합니다.
- 전송 시작 직전에 `System.nanoTime()`/`Instant.now()`로 시간을 기록하고, `SendResult`가 돌아온 시점에서 다시 기록하여 소요 시간을 계산합니다.
- 기대 효과: 지연(`linger`)을 늘리면 개별 레이턴시는 증가하지만 처리량이 향상되는지 확인할 수 있습니다.

## 2. JsonSerializer vs 커스텀 Serializer
- 기본 `JsonSerializer` 설정과 직접 구현한 Serializer(예: Jackson `ObjectMapper` 래핑, `kotlinx.serialization` 기반)를 번갈아 적용합니다.
- 각 Serializer별로 전송 메시지 크기(바이트)를 Kafka UI 혹은 `kafka-console-consumer`에서 확인하고 전송 소요 시간도 함께 기록합니다.
- 기대 효과: 기본 Serializer 대비 커스텀 구현이 직렬화/역직렬화 속도, 메시지 크기 측면에서 어떤 차이가 있는지 파악할 수 있습니다.

## 3. 압축 알고리즘 비교
- `compression.type`을 `none`, `snappy`, `lz4`, `gzip`, `zstd`로 순차 변경하며 동일한 배치 전송을 반복합니다.
- 전송 시간, Kafka 브로커의 CPU 사용량(`docker stats` 활용), 저장된 메시지 크기 등을 표로 정리합니다.
- 기대 효과: 압축률과 CPU 부담 사이의 Trade-off를 정량적으로 이해할 수 있습니다.

## 4. Key Serializer에 따른 파티셔닝 변화
- 현재 `StringSerializer` 대신 숫자형(`LongSerializer`)이나 UUID 전용 Serializer를 사용해 봅니다.
- Kafka UI 토픽 상세 화면에서 파티션별 메시지 분포와 레이턴시를 비교합니다.
- 기대 효과: 키 타입에 따라 파티셔닝과 처리량이 어떻게 변하는지 확인할 수 있습니다.

## 5. Avro / Protobuf 실험
- Schema Registry를 활용해 Avro Serializer(`KafkaAvroSerializer`)로 전환하고, `GenericRecord`와 `SpecificRecord`를 각각 테스트합니다.
- 스키마 변경(필드 추가/삭제)에 따른 Schema Registry 호환 모드(`BACKWARD`, `FORWARD`, `FULL`) 차이를 실험하고, 호환성 위반 시 어떤 에러가 발생하는지 기록합니다.
- 기대 효과: 강한 스키마 기반 Serializer의 장단점과 스키마 진화 전략을 체감할 수 있습니다.

## 퍼포먼스 측정 팁
- 전송 측: `CompletableFuture` 완료 시간, Producer metrics(`client-id` 기반) 활용.
- 소비 측: 전용 컨슈머 또는 `kafka-console-consumer`로 레이턴시와 메시지를 확인.
- 외부 도구: `k6`, `wrk` 등으로 REST API에 부하를 주고 처리량을 측정.
- JVM 모니터링: 메시지 크기가 큰 경우 Heap 사용량(GC 로그, VisualVM 등)도 관찰해 보세요.

---
위 실험을 진행하면서 궁금한 설정이나 추가 측정 지표가 필요하면 언제든지 요청해 주세요.

AdClickProducer summary serializer=SPRING_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=181.35 avgDurationMillis=3.436 totalSizeBytes=181347890 totalDurationMillis=3436416.307
AdClickProducer summary serializer=SPRING_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=181.35 avgDurationMillis=3.030 totalSizeBytes=181347890 totalDurationMillis=3029687.170
AdClickProducer summary serializer=SPRING_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=181.35 avgDurationMillis=2.956 totalSizeBytes=181347890 totalDurationMillis=2956009.903
AdClickProducer summary serializer=SPRING_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=181.35 avgDurationMillis=2.947 totalSizeBytes=181347890 totalDurationMillis=2947144.033
AdClickProducer summary serializer=SPRING_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=181.35 avgDurationMillis=3.019 totalSizeBytes=181347890 totalDurationMillis=3018893.744

AdClickProducer summary serializer=JACKSON_OBJECT_MAPPER trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=3.202 totalSizeBytes=190344849 totalDurationMillis=3201744.897
AdClickProducer summary serializer=JACKSON_OBJECT_MAPPER trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.35 avgDurationMillis=2.795 totalSizeBytes=190345034 totalDurationMillis=2794933.224
AdClickProducer summary serializer=JACKSON_OBJECT_MAPPER trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=2.888 totalSizeBytes=190344880 totalDurationMillis=2888144.269
AdClickProducer summary serializer=JACKSON_OBJECT_MAPPER trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=2.919 totalSizeBytes=190344970 totalDurationMillis=2919462.359
AdClickProducer summary serializer=JACKSON_OBJECT_MAPPER trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=2.993 totalSizeBytes=190344880 totalDurationMillis=2993105.897

AdClickProducer summary serializer=KOTLINX_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=2.950 totalSizeBytes=190344928 totalDurationMillis=2950452.538
AdClickProducer summary serializer=KOTLINX_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=2.914 totalSizeBytes=190344872 totalDurationMillis=2913595.245
AdClickProducer summary serializer=KOTLINX_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=2.833 totalSizeBytes=190344857 totalDurationMillis=2832513.247
AdClickProducer summary serializer=KOTLINX_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.35 avgDurationMillis=2.887 totalSizeBytes=190345020 totalDurationMillis=2886529.231
AdClickProducer summary serializer=KOTLINX_JSON trigger=load-test:batch success=1000000 failure=0 avgSizeBytes=190.34 avgDurationMillis=2.865 totalSizeBytes=190344908 totalDurationMillis=2865257.177