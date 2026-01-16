# payment-app 프로젝트 가이드

## 1) 프로젝트 개요
- 결제 도메인 서버 예제입니다.
- 멀티모듈 + 헥사고널 구조를 유지합니다.

## 2) 모듈 구성
- `modules/domain`: 도메인 모델/유틸리티
- `modules/application`: 유스케이스/포트 정의
- `modules/infrastructure`: 영속성 어댑터
- `modules/external`: 외부 PG 클라이언트
- `modules/bootstrap`: 실행 가능한 API

## 3) 실행 환경
- JDK 21
- Gradle Wrapper

## 4) 실행
```bash
./gradlew :modules:bootstrap:api-payment-gateway:bootRun
```
- 기본 포트: `8080`

## 5) 테스트
```bash
./gradlew test
```

## 6) 커버리지(JaCoCo)
```bash
./gradlew :modules:domain:jacocoTestReport
```
- 리포트 위치: `modules/<모듈>/build/reports/jacoco/test/html/index.html`
- 커버리지 리포트를 통해 모듈별 안정성/테스트 범위를 확인할 수 있습니다.

## 7) API 요약
### 7.1 결제 생성
- `POST /api/v1/payments`

### 7.2 결제 조회
- `GET /api/v1/payments`

## 8) 운영 메트릭(Actuator)
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,prometheus"
```
- Prometheus: `http://localhost:8080/actuator/prometheus`

## 9) Docker Compose 예시(로컬 운영 스택)
### 9.1 docker-compose.yml
```yaml
version: "3.8"
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    depends_on:
      - prometheus

  mariadb:
    image: mariadb:11
    environment:
      MARIADB_DATABASE: payment
      MARIADB_USER: payment_user
      MARIADB_PASSWORD: payment_pass
      MARIADB_ROOT_PASSWORD: root_pass
    ports:
      - "3306:3306"
```

### 9.2 prometheus.yml
```yaml
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: "payment-app"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["host.docker.internal:8080"]
```

### 9.3 실행
```bash
docker compose up -d
```

## 10) 아키텍처/의존성 검증
- ArchUnit 기반 테스트로 계층 간 의존성 방향이 지켜지는지 검증합니다.
- 관련 테스트: `modules/bootstrap/api-payment-gateway/src/test/kotlin/im/bigs/pg/arch/ArchitectureTest.kt`

## 11) 주요 테스트 사항
- 아키텍처 테스트로 계층 간 의존성 규칙 준수를 지속적으로 검증합니다.
- 커버리지 리포트를 통해 코드 안정성과 테스트 범위를 점검할 수 있습니다.
- 결제 생성/조회 유스케이스 단위 테스트로 핵심 도메인 흐름을 검증합니다.
- 커서 기반 페이지네이션/요약 집계 로직을 테스트로 확인합니다.
- DTO/응답 매핑 테스트로 API 계층 변환 로직을 검증합니다.

