# 작업 진행 요약

## 브랜치
- `feature-producer-batch`

## 주요 구현 항목
- Kafka 프로듀서 도메인/설정/서비스 구성 (`AdClickEvent`, `AdClickProducer`, `KafkaProducerConfig`)
- REST API(`/api/ad-clicks`) 및 DTO(`AdClickRequest`, `AdClickResponse`) 추가
- IntelliJ HTTP 호출 파일 `src/main/http/ad-click.http` 작성
- 프로듀서 설정을 외부화하기 위한 `AdClickProducerProperties` 작성 및 Bean Validation 도입
- 로드 테스트 설정 `AdClickLoadTestProperties` 및 실행 러너 `AdClickLoadTestRunner` 추가
- 단일/배치 비교를 위한 프로필 구성(`application-single.yml`, `application-batch.yml`)
- Serializer 학습 가이드 (`docs/serializer-learning-guide.md`) 및 벤치 계획 (`docs/batch-vs-single-benchmark.md`) 문서화

## 의존성 추가
- `spring-boot-starter-validation` (Bean Validation 지원)

## 실행 결과
- `./gradlew bootRun --args='--spring.profiles.active=single'` 실행 시 `AdClickLoadTestRunner`가 100,000건 전송 완료
  - 로그: `Completed ad click load test: scenario=single, totalEvents=100000, duration=66240 ms`
- 동일 방식으로 `batch` 프로필 실행 예정

## 다음 단계 제안
1. `batch` 프로필 실행 후 결과를 `docs/batch-vs-single-benchmark.md`에 기록
2. Serializer 유형/압축 설정을 변경하며 벤치마크 확대
3. 컨슈머 코드 추가 및 end-to-end 검증 (필요 시)
