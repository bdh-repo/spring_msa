# MSA (Microservices Architecture) Tutorial Project

Spring Boot와 Spring Cloud를 이용한 마이크로서비스 아키텍처 학습 프로젝트입니다.

## 📋 목차
- [MSA란?](#msa란)
- [프로젝트 구조](#프로젝트-구조)
- [서비스별 상세 설명](#서비스별-상세-설명)
- [MSA 동작 흐름](#msa-동작-흐름)
- [실행 방법](#실행-방법)
- [API 테스트](#api-테스트)

## 🏗️ MSA란?

**MSA(Microservices Architecture)**는 하나의 큰 애플리케이션을 여러 개의 작은, 독립적인 서비스로 나누어 개발하는 아키텍처 패턴입니다.

### 기존 모놀리식 vs MSA
- **모놀리식**: 모든 기능이 하나의 애플리케이션에 통합
- **MSA**: 각 기능을 독립적인 서비스로 분리

### MSA 장점
- ✅ 서비스별 독립 배포/확장 가능
- ✅ 기술 스택 다양화 가능  
- ✅ 장애 격리 (한 서비스 장애가 전체에 영향 최소화)
- ✅ 팀별 독립 개발 가능

## 🏛️ 프로젝트 구조

```
tutorial (루트 프로젝트)
├── eureka-server      # 서비스 디스커버리 (포트: 8761)
├── api-gateway        # API 게이트웨이 (포트: 8080)
├── member-service     # 회원 관리 서비스 (포트: 8081)
└── board-service      # 게시판 서비스 (포트: 8082)
```

### 기술 스택
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Build Tool**: Gradle
- **MSA Components**: Spring Cloud (Eureka, Gateway, OpenFeign)

## 🔧 서비스별 상세 설명

### 1. Eureka Server (서비스 레지스트리)
- **포트**: 8761
- **역할**: 모든 서비스의 위치 정보 관리
- **주요 기능**:
  - 서비스 등록: 각 마이크로서비스가 자신의 위치(IP, 포트)를 등록
  - 서비스 발견: 다른 서비스들이 필요한 서비스의 위치를 조회
  - 헬스체크: 등록된 서비스들의 상태를 주기적으로 확인
  - 로드밸런싱: 동일한 서비스의 여러 인스턴스 중 적절한 인스턴스 선택

### 2. API Gateway (단일 진입점)
- **포트**: 8080
- **역할**: 모든 외부 요청의 진입점 및 라우팅
- **라우팅 규칙**:
  - `/api/members/**` → member-service
  - `/api/boards/**` → board-service
- **주요 기능**:
  - 라우팅: 클라이언트 요청을 적절한 마이크로서비스로 전달
  - 로드밸런싱: 동일한 서비스의 여러 인스턴스에 요청 분산
  - 인증/인가: 보안 기능 중앙화
  - 로깅/모니터링: 요청 추적 및 성능 모니터링

### 3. Member Service (회원 서비스)
- **포트**: 8081
- **책임**: 회원 정보 관리 (CRUD), 회원 인증/인가, 회원 프로필 관리
- **API 엔드포인트**:
  - `GET /members/{id}` - 회원 조회
  - `POST /members` - 회원 가입
  - `PUT /members/{id}` - 회원 수정
  - `DELETE /members/{id}` - 회원 탈퇴
  - `GET /members/health` - 헬스체크

### 4. Board Service (게시판 서비스)
- **포트**: 8082
- **책임**: 게시글 관리, Member Service와의 연동
- **특징**: Feign Client를 통해 Member Service와 통신
- **API 엔드포인트**:
  - `GET /boards` - 모든 게시글 조회
  - `GET /boards/{id}` - 특정 게시글 조회
  - `POST /boards?memberId={id}` - 게시글 작성 (회원 정보 연동)
  - `PUT /boards/{id}` - 게시글 수정
  - `DELETE /boards/{id}` - 게시글 삭제

## 🔄 MSA 동작 흐름

### 1. 서비스 시작 흐름
```
1. Eureka Server (8761) 시작
   ↓
2. 각 서비스들이 Eureka Server에 자신을 등록
   (member-service, board-service, api-gateway)
   ↓
3. 모든 서비스가 서로의 위치를 알 수 있게 됨
```

### 2. 클라이언트 요청 흐름
```
클라이언트 요청
   ↓
API Gateway (8080) - 단일 진입점
   ↓
라우팅 규칙에 따라 적절한 서비스로 전달
   ↓
Member Service (8081) 또는 Board Service (8082)
```

**예시: 회원 조회 요청**
```
GET /api/members/1
   ↓
API Gateway → Member Service
   ↓
응답: 회원 ID 1의 정보
```

### 3. 서비스 간 통신 흐름 (게시글 작성)
```
1. 클라이언트 → API Gateway → Board Service
2. Board Service → Eureka에서 Member Service 위치 조회
3. Board Service → Feign Client로 Member Service 호출
4. Member Service → 회원 정보 반환
5. Board Service → 게시글 생성 및 결과 반환
```

## 🚀 실행 방법

### 1. 필수 요구사항
- Java 17
- Gradle 7.0+

### 2. 서비스 실행 순서 (중요!)
```bash
# 1. Eureka Server 먼저 실행
cd eureka-server
./gradlew bootRun

# 2. Member Service 실행
cd ../member-service  
./gradlew bootRun

# 3. Board Service 실행
cd ../board-service
./gradlew bootRun

# 4. API Gateway 마지막 실행
cd ../api-gateway
./gradlew bootRun
```

### 3. 서비스 상태 확인
- **Eureka Dashboard**: http://localhost:8761
- **Member Service Health**: http://localhost:8081/members/health
- **Board Service Health**: http://localhost:8082/boards/health

## 🧪 API 테스트

### Member Service API (직접 호출)
```bash
# 회원 조회
curl http://localhost:8081/members/1

# 회원 가입
curl -X POST http://localhost:8081/members \
  -H "Content-Type: application/json" \
  -d "회원가입 데이터"
```

### API Gateway를 통한 호출 (권장)
```bash
# 회원 조회 (API Gateway 경유)
curl http://localhost:8080/api/members/1

# 게시글 작성 (서비스 간 통신 테스트)
curl -X POST "http://localhost:8080/api/boards?memberId=1" \
  -H "Content-Type: application/json" \
  -d "게시글 내용"
```

## 📝 핵심 MSA 패턴

### 1. Service Discovery Pattern
- **Eureka**: 서비스 위치 자동 관리
- 서비스들이 IP/포트 대신 서비스 이름으로 통신

### 2. API Gateway Pattern  
- **단일 진입점**: 모든 외부 요청을 API Gateway가 처리
- **라우팅 중앙화**: 요청 경로에 따라 적절한 서비스로 전달

### 3. Service Communication Pattern
- **Feign Client**: 선언적 HTTP 클라이언트로 서비스 간 통신
- **Load Balancing**: `lb://service-name`으로 자동 로드밸런싱

### 4. Circuit Breaker Pattern (고급)
- 서비스 장애 시 연쇄 장애 방지
- 향후 추가 구현 예정

## 🎯 학습 포인트

1. **서비스 분리**: 각 서비스가 독립적인 책임을 가짐
2. **서비스 디스커버리**: 고정 IP 대신 서비스 이름으로 통신
3. **API Gateway**: 외부 세계와의 단일 접점
4. **서비스 간 통신**: REST API와 Feign Client 활용
5. **확장성**: 서비스별 독립적인 스케일링 가능

---

이 프로젝트는 MSA의 기본 개념들을 학습하기 위한 튜토리얼입니다. 실제 운영 환경에서는 보안, 모니터링, 로깅 등 추가 고려사항들이 필요합니다.