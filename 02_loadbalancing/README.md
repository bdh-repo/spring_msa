# 📚 MSA 로드밸런싱 완벽 가이드

## 🎯 목차
1. [MSA 로드밸런싱 핵심 개념](#1-msa-로드밸런싱-핵심-개념)
2. [Spring Cloud 생태계 이해](#2-spring-cloud-생태계-이해)
3. [로드밸런싱 실습: Board Service 다중 인스턴스 구성](#3-로드밸런싱-실습-board-service-다중-인스턴스-구성)
4. [심화 개념 및 운영 고려사항](#4-심화-개념-및-운영-고려사항)
5. [학습 포인트 및 다음 단계](#5-학습-포인트-및-다음-단계)

---

## 💡 1. MSA 로드밸런싱 핵심 개념

### 🔹 1.1 로드밸런서란?
- **정의**: 여러 서버 인스턴스에 네트워크 트래픽을 효율적으로 분산시키는 장치/소프트웨어
- **목적**: 
  - 서버 부하 분산으로 응답 속도 향상
  - 단일 장애점(Single Point of Failure) 제거
  - 시스템 가용성 및 확장성 향상

### 🔹 1.2 MSA에서 로드밸런싱의 중요성
- 마이크로서비스는 독립적으로 확장 가능
- 특정 서비스에 트래픽 집중 시 해당 서비스만 스케일 아웃
- 장애 발생 시 다른 인스턴스로 자동 전환하여 서비스 지속성 보장

### 🔹 1.3 로드밸런싱 알고리즘
- **Round Robin**: 순차적으로 요청 분배 (기본값)
- **Weighted Round Robin**: 서버 성능에 따라 가중치 부여
- **Least Connections**: 연결 수가 가장 적은 서버 선택
- **Random**: 무작위로 서버 선택
- **Health Check 기반**: 정상 상태인 서버만 선택

### 🔹 1.4 L4 vs L7 로드밸런싱
- **L4 (Transport Layer)**: IP + Port 기반 분배
  - 빠른 처리 속도
  - TCP/UDP 프로토콜 레벨
  
- **L7 (Application Layer)**: HTTP 헤더, URL 기반 분배
  - 세밀한 트래픽 제어
  - Content-based routing 가능
  - Spring Cloud Gateway는 L7 로드밸런서

---

## 🌐 2. Spring Cloud 생태계 이해

### 🔹 2.1 Spring Cloud 아키텍처

```
┌─────────────────┐    ┌─────────────────┐
│   Client        │────│  API Gateway    │
│   (Browser/App) │    │  (Port 8080)    │
└─────────────────┘    └─────────┬───────┘
                                 │ Load Balancing
                                 ▼
              ┌─────────────────────────────────────┐
              │        Service Discovery            │
              │        (Eureka Server)              │
              │         (Port 8761)                 │
              └─────────┬───────────────────────────┘
                        │ Service Registration
                        ▼
        ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
        │ Member Service│  │Board Service  │  │Board Service  │
        │  (Port 8081)  │  │ (Port 8082)   │  │ (Port 8084)   │
        └───────────────┘  └───────────────┘  └───────────────┘
```

### 🔹 2.2 핵심 컴포넌트 역할
- **API Gateway (Spring Cloud Gateway)**
  - 단일 진입점 (Single Entry Point)
  - 클라이언트 요청을 적절한 마이크로서비스로 라우팅
  - 로드밸런싱, 인증, 로깅 등 횡단 관심사 처리

- **Service Discovery (Netflix Eureka)**
  - 서비스 등록/발견
  - 서비스 인스턴스의 위치 정보 관리
  - 헬스체크를 통한 서비스 상태 모니터링

- **Load Balancer (Spring Cloud LoadBalancer)**
  - 서비스 인스턴스 간 트래픽 분산
  - 장애 인스턴스 자동 제외

### 🔹 2.3 자동화된 헬스체크 매커니즘
- Eureka 클라이언트가 30초마다 heartbeat 전송
- 90초 동안 응답 없으면 인스턴스 제거
- API Gateway가 실시간으로 살아있는 인스턴스만 선택
- 장애 복구 시 자동으로 다시 로드밸런싱 풀에 포함

### 🔹 2.4 서비스 디스커버리 동작 과정
1. 서비스 시작 시 Eureka Server에 자신의 정보 등록 (서비스명, IP, 포트, 상태 등)
2. API Gateway가 Eureka에서 서비스 목록 조회
3. 클라이언트 요청 시 lb:// prefix로 로드밸런싱 수행
4. 주기적 헬스체크로 서비스 상태 업데이트

---

## 🛠️ 3. 로드밸런싱 실습: Board Service 다중 인스턴스 구성

### 📋 실습 목표
- board-service를 8082, 8084 두 포트로 실행
- API Gateway의 로드밸런싱 동작 확인
- 요청이 두 인스턴스에 골고루 분산되는지 검증

### 🔧 Step 1: 코드 수정 (포트 정보 표시)

#### 1.1 application.yml 환경변수 설정
`board-service/src/main/resources/application.yml`:

```yaml
server:
  port: ${SERVER_PORT:8082}  # 환경변수 우선, 기본값 8082
```

#### 1.2 Controller에 포트 정보 추가
`BoardController.java`에 다음 코드 추가:

```java
@Value("${server.port}")
private String serverPort;

@GetMapping
public String getAllBoards() {
    return "Board Service (Port: " + serverPort + "): 모든 게시글 목록을 조회했습니다.";
}

@GetMapping("/{id}")
public String getBoard(@PathVariable String id) {
    return "Board Service (Port: " + serverPort + "): 게시글 ID " + id + "를 조회했습니다.";
}

@GetMapping("/health")
public String health() {
    return "Board Service is running on port " + serverPort;
}
```

### ⚙️ Step 2: IntelliJ 실행 구성 생성

#### 2.1 첫 번째 실행 구성 (기본 - 8082 포트)
- `BoardServiceApplication.java` 우클릭
- "Run 'BoardServiceApplication'" 선택하여 기본 구성 생성

#### 2.2 두 번째 실행 구성 생성 (8084 포트)
- 상단 메뉴: Run → Edit Configurations...
- 좌측에서 BoardServiceApplication 선택
- 복사 버튼 클릭 (Copy Configuration)
- 새 구성 설정:
  - Name: BoardService-8084
  - Environment variables: SERVER_PORT=8084 추가
  - Apply → OK

#### 2.3 실행 구성 확인
실행 구성 드롭다운에서 다음 두 개가 보여야 함:
- BoardServiceApplication (8082 포트)
- BoardService-8084 (8084 포트)

### 🚀 Step 3: 다중 인스턴스 실행

#### 3.1 필수 서비스 실행 순서
1. EurekaServerApplication (8761)
2. ApiGatewayApplication (8080)
3. MemberServiceApplication (8081) - 참조용

#### 3.2 Board Service 두 인스턴스 실행
- 첫 번째: BoardServiceApplication 실행 (8082)
- 두 번째: BoardService-8084 실행 (8084)

#### 3.3 실행 상태 확인
IntelliJ Services 탭에서 두 인스턴스가 모두 실행 중인지 확인

### 🔍 Step 4: 로드밸런싱 테스트

#### 4.1 Eureka Dashboard 확인
- http://localhost:8761 접속
- BOARD-SERVICE가 2개 인스턴스로 등록되었는지 확인
- 각각 8082, 8084 포트로 등록되어 있어야 함

#### 4.2 API Gateway를 통한 요청 테스트

**방법 1: curl 명령어**
```bash
curl http://localhost:8080/api/boards
curl http://localhost:8080/api/boards/1
curl http://localhost:8080/api/boards/2
curl http://localhost:8080/api/boards/3
curl http://localhost:8080/api/boards/4
```

**방법 2: 브라우저 테스트**
- http://localhost:8080/api/boards 접속
- 새로고침(F5) 여러 번 실행
- 응답에서 포트 번호가 8082 ↔ 8084로 번갈아가며 변경되는지 확인

#### 4.3 예상 응답 결과
```
Board Service (Port: 8082): 모든 게시글 목록을 조회했습니다.
Board Service (Port: 8084): 모든 게시글 목록을 조회했습니다.
Board Service (Port: 8082): 게시글 ID 1를 조회했습니다.
Board Service (Port: 8084): 게시글 ID 2를 조회했습니다.
```

### 📊 Step 5: 로드밸런싱 알고리즘 확인

#### 5.1 기본 알고리즘
Spring Cloud LoadBalancer는 기본적으로 Round Robin 방식:
- 첫 번째 요청 → 8082 포트
- 두 번째 요청 → 8084 포트
- 세 번째 요청 → 8082 포트
- 네 번째 요청 → 8084 포트

#### 5.2 로그 확인
API Gateway 콘솔에서 다음 로그 확인 가능:
```
RouteDefinition matched: board-service
LoadBalancer: Selecting instance for board-service
```

### ✅ 성공 기준
다음 조건이 모두 만족되면 로드밸런싱이 정상 동작:
- ☑ Eureka Dashboard에서 board-service가 2개 인스턴스로 표시
- ☑ API 요청 시 포트가 8082 ↔ 8084로 번갈아가며 응답
- ☑ 한쪽 인스턴스를 중단해도 다른 인스턴스로 자동 라우팅
- ☑ 중단된 인스턴스를 재시작하면 다시 로드밸런싱에 포함

---

## 🔧 4. 심화 개념 및 운영 고려사항

### 🔹 4.1 트러블슈팅

**문제 1**: 두 번째 인스턴스가 시작되지 않음
- 원인: 포트 충돌
- 해결: Environment variables에 SERVER_PORT=8084 정확히 설정

**문제 2**: 요청이 한 포트로만 감
- 원인: Eureka 등록 지연
- 해결: 30초 정도 대기 후 재테스트

**문제 3**: Eureka에서 인스턴스가 보이지 않음
- 원인: 네트워크 또는 설정 문제
- 해결: application.yml에서 eureka.client.defaultZone 확인

### 🔹 4.2 로드밸런서 설정 커스터마이징

**로드밸런싱 알고리즘 변경**
```java
@Bean
@LoadBalanced
public ReactorLoadBalancerExchangeFilterFunction lbFunction() {
    return new ReactorLoadBalancerExchangeFilterFunction(
        LoadBalancerClientFactory.create(RandomLoadBalancer.class));
}
```

**헬스체크 주기 조정**
```yaml
eureka:
  instance:
    lease-renewal-interval-in-seconds: 10    # 10초마다 헬스체크
    lease-expiration-duration-in-seconds: 30 # 30초 후 만료
```

### 🔹 4.3 운영 환경 고려사항
- **Circuit Breaker 패턴**: 장애 서비스 격리
- **Sticky Session**: 사용자 세션 유지가 필요한 경우
- **지역별 로드밸런싱**: 다중 리전 환경에서 레이턴시 최적화
- **모니터링**: Prometheus, Grafana를 통한 실시간 모니터링

### 🔹 4.4 확장성 및 성능 최적화
- **Auto Scaling**: CPU, 메모리 사용률에 따른 자동 확장
- **Connection Pool**: 데이터베이스 연결 최적화
- **Caching**: Redis를 통한 응답 캐싱
- **CDN 연동**: 정적 리소스 배포 최적화

### 🔹 4.5 보안 고려사항
- **API Gateway에서 인증/인가 처리**
- **SSL/TLS 적용으로 통신 암호화**
- **Rate Limiting**: 과도한 요청 차단
- **IP Whitelist**: 특정 IP만 접근 허용

---

## 🎯 5. 학습 포인트 및 다음 단계

### 📚 이 실습을 통해 학습한 내용
- **MSA의 확장성**: 동일한 서비스를 여러 인스턴스로 실행
- **자동 로드밸런싱**: Spring Cloud Gateway의 자동 부하 분산
- **서비스 디스커버리**: Eureka를 통한 동적 서비스 발견
- **무중단 서비스**: 인스턴스 하나가 다운되어도 서비스 지속

### 🚀 다음 학습 주제
- **Circuit Breaker 패턴 적용** (Netflix Hystrix/Resilience4j)
- **분산 추적** (Sleuth + Zipkin)
- **API Gateway 인증/인가** (JWT, OAuth2)
- **컨테이너 오케스트레이션** (Docker, Kubernetes)
- **모니터링 및 로깅** (ELK Stack)

### 💡 실무 적용 팁
- 처음에는 단순한 Round Robin으로 시작
- 운영 중 서비스별 특성에 따라 로드밸런싱 알고리즘 조정
- 헬스체크 실패 시 빠른 대응을 위한 모니터링 체계 구축
- 장애 상황을 가정한 카오스 엔지니어링 도입 검토

---

## 🎉 축하합니다!

MSA 로드밸런싱의 핵심 개념을 완전히 이해하고 실습까지 완료하셨습니다.
이제 실제 운영 환경에서도 확장 가능하고 안정적인 MSA 시스템을 구축할 수 있는 기반을 갖추셨습니다!