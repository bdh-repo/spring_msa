# CLAUDE.md

MSA(Microservices Architecture) 보안 프로젝트를 통한 실습 학습 가이드

## 프로젝트 개요

MSA 보안 패턴과 구현을 학습하기 위한 실무 중심 프로젝트입니다. JWT 기반 인증, API Gateway 보안, 서비스 간 통신 보안 등을 실습하며 MSA의 핵심 보안 요소들을 익힙니다.

## MSA 기초 공부 커리큘럼

### 📚 Phase 1: MSA 기본 개념 및 아키텍처 (1-2주)
- [ ] **1.1 MSA 개념 이해**
  - Monolith vs MSA 비교
  - MSA의 장단점
  - 언제 MSA를 선택해야 하는가?

- [ ] **1.2 MSA 핵심 패턴**
  - Service Discovery
  - API Gateway 패턴
  - Circuit Breaker 패턴
  - Saga 패턴 (분산 트랜잭션)

- [ ] **1.3 서비스 분해 전략**
  - Domain-Driven Design (DDD) 기초
  - Bounded Context
  - 서비스 경계 설정

### 🔐 Phase 2: MSA 보안 기초 (2-3주)
- [ ] **2.1 MSA 보안 모델**
  - Zero Trust Architecture
  - Service-to-Service 인증
  - 보안 경계 설정

- [ ] **2.2 인증 및 인가**
  - JWT 기반 인증 구현
  - OAuth 2.0 / OpenID Connect
  - API Gateway에서의 인증 처리

- [ ] **2.3 서비스 간 통신 보안**
  - mTLS (mutual TLS) 구현
  - Service Mesh 보안
  - API 암호화

### 🏗️ Phase 3: 실무 구현 (3-4주)
- [ ] **3.1 프로젝트 설정**
  - Spring Boot 기반 마이크로서비스 구성
  - Docker 컨테이너화
  - 로컬 개발 환경 구축

- [ ] **3.2 핵심 서비스 구현**
  - User Service (사용자 관리)
  - Auth Service (인증 서비스)
  - Product Service (비즈니스 로직)
  - Gateway Service (API 게이트웨이)

- [ ] **3.3 보안 기능 구현**
  - JWT 토큰 기반 인증 시스템
  - API Gateway 보안 필터
  - 서비스별 권한 관리
  - 보안 로깅 및 모니터링

### 🔄 Phase 4: 고급 패턴 및 운영 (2-3주)
- [ ] **4.1 데이터 관리**
  - Database per Service 패턴
  - Event Sourcing
  - CQRS (Command Query Responsibility Segregation)

- [ ] **4.2 운영 및 모니터링**
  - 분산 로깅 (ELK Stack)
  - 분산 추적 (Zipkin/Jaeger)
  - 메트릭 수집 및 알람

- [ ] **4.3 배포 및 DevOps**
  - CI/CD 파이프라인 구성
  - Kubernetes 배포
  - Blue-Green 배포 전략

## 🎯 학습 진행도

### 현재 진행 상황
- ✅ **프로젝트 초기 설정 완료** (2025-07-21)
- 🔄 **Phase 1: MSA 기본 개념 학습 중**

### 완료된 마일스톤
| 날짜 | 완료 항목 | 비고 |
|------|-----------|------|
| 2025-07-21 | 프로젝트 구조 설정 | 기본 프로젝트 템플릿 생성 |

### 다음 목표
1. MSA 기본 개념 이해 및 정리
2. 첫 번째 마이크로서비스 구현 (User Service)
3. 기본 API Gateway 설정

## 📝 학습 노트

### 주요 개념 정리
> 학습하면서 중요한 개념들을 여기에 기록해주세요.

### 구현 중 발견한 이슈
> 구현 과정에서 마주한 문제와 해결책을 기록해주세요.

### 참고 자료
- [Spring Cloud 공식 문서](https://spring.io/projects/spring-cloud)
- [MSA 패턴 가이드](https://microservices.io/patterns/)
- [JWT 보안 가이드](https://jwt.io/introduction)

## 🛠️ 개발 환경 및 도구

### 필수 도구
- Java 17+
- Spring Boot 3.x
- Docker & Docker Compose
- Maven
- IntelliJ IDEA (권장)

### 사용 기술 스택
- **Backend**: Spring Boot, Spring Cloud Gateway, Spring Security
- **Database**: PostgreSQL, Redis
- **Message Queue**: RabbitMQ
- **Monitoring**: Prometheus, Grafana
- **Container**: Docker, Kubernetes

---

**💡 팁**: 각 Phase를 완료할 때마다 이 파일의 진행도를 업데이트하고, 학습한 내용을 정리해주세요!