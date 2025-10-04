# 단일 전송 vs 배치 전송 실험 계획

## 1. 주요 작업 정리
- `AdClickProducer` 기반 Kafka 전송 로직에 학습용 주석 추가
- REST API(`/api/ad-clicks`)를 이용해 외부에서 이벤트를 전송 가능하도록 구현
- IntelliJ HTTP 호출 파일(`src/main/http/ad-click.http`)로 빠른 수동 검증 지원
- 단일/배치 전송 비교를 위한 Spring Profile(`single`, `batch`) 구성
- `AdClickLoadTestRunner`를 통해 기동 시 100,000건 이벤트를 일괄 전송할 수 있도록 구현
- Serializer 학습 가이드를 `docs/serializer-learning-guide.md`에 정리하여 다양한 실험 방향 제시

## 2. 프로필별 Kafka 프로듀서 설정 요약
| 항목 | single 프로필 | batch 프로필 |
| --- | --- | --- |
| `linger.ms` | `0` | `50` |
| `batch.size` | `16384` (16KB) | `65536` (64KB) |
| `compression.type` | `none` | `snappy` |
| `enable.idempotence` | `false` | `true` |
| `scenario-label` | `single` | `batch` |

## 3. 실행 방법
```bash
# 단일 전송 시나리오 (즉시 전송, 압축 없음)
./gradlew bootRun --args='--spring.profiles.active=single'

# 배치 전송 시나리오 (배치/압축 활성)
./gradlew bootRun --args='--spring.profiles.active=batch'
```

애플리케이션 기동 시 `AdClickLoadTestRunner`가 자동으로 100,000건을 전송하고 로그에 소요 시간과 시나리오 정보를 출력합니다.

## 4. 벤치마크 기록 포맷 제안
- 실행 시각, 시나리오(`single`, `batch`)
- 총 전송 건수, 걸린 시간(ms)
- 초당 처리량(건/초)
- Kafka 브로커 CPU/메모리 사용량 (`docker stats` 기반)
- 전송 메시지 평균 크기 (Kafka UI or `kafka-console-consumer`)

예시 표:
```
| 실행일시 | 시나리오 | 총건수 | 소요시간(ms) | 처리량(건/초) | 비고 |
|---------|----------|--------|--------------|---------------|------|
| 2025-10-05 11:00 | single | 100000 | 52000 | 1923 | 압축 없음 |
| 2025-10-05 11:30 | batch  | 100000 | 21000 | 4761 | snappy 압축 |
```

## 5. 추가 실험 아이디어
- `compression.type`을 `none`, `snappy`, `lz4`, `gzip`으로 순차 변경하여 비교
- Avro Serializer로 전환 후 Schema Registry 활용 실험
- Key Serializer를 `String`, `UUID`, `Long` 등으로 바꿔 파티션 분포 비교
- `spring.kafka.producer.properties`에 `max.in.flight.requests.per.connection`, `retries` 등을 조절해 재전송 동작 관찰

필요 시 추가 실험 항목이나 측정 스크립트를 함께 정리해 나갈 수 있습니다.
