# MSA JWT Security 구현 교안

## 📚 목차

1. [MSA 보안의 핵심 개념](#1-msa-보안의-핵심-개념)
2. [JWT 기반 인증 시스템](#2-jwt-기반-인증-시스템)
3. [구현 아키텍처](#3-구현-아키텍처)
4. [단계별 구현 가이드](#4-단계별-구현-가이드)
5. [보안 고려사항](#5-보안-고려사항)
6. [테스트 및 검증](#6-테스트-및-검증)

---

## 1. MSA 보안의 핵심 개념

### 1.1 MSA 환경에서의 보안 도전 과제

**전통적인 모놀리식 vs MSA 보안**
```
모놀리식 애플리케이션:
┌─────────────────────┐
│   Single Application │
│  ┌─────────────────┐ │
│  │   Web Layer     │ │ ← 단일 진입점에서 인증
│  │   Service Layer │ │
│  │   Data Layer    │ │
│  └─────────────────┘ │
└─────────────────────┘

MSA 환경:
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Service A  │  │  Service B  │  │  Service C  │
└─────────────┘  └─────────────┘  └─────────────┘
       ↑                ↑                ↑
       └────────────────┼────────────────┘
                        ↑
              ┌─────────────────┐
              │   API Gateway   │ ← 중앙 집중식 인증 필요
              └─────────────────┘
```

**MSA 보안의 핵심 원칙**
- **중앙 집중식 인증**: API Gateway에서 통합 인증 처리
- **토큰 기반 인증**: Stateless한 JWT 토큰 사용
- **서비스별 권한 분리**: 각 서비스의 독립적인 권한 관리
- **Zero Trust**: 모든 서비스 간 통신에 대한 검증

### 1.2 JWT(JSON Web Token)의 필요성

**세션 vs JWT 비교**

| 구분 | 세션 기반 | JWT 기반 |
|------|-----------|----------|
| 상태 관리 | Stateful (서버에 세션 저장) | Stateless (클라이언트에 정보 저장) |
| 확장성 | 세션 클러스터링 필요 | 서버 확장 용이 |
| 서비스 간 통신 | 세션 공유 복잡 | 토큰 전달로 간단 |
| 메모리 사용 | 서버 메모리 사용 | 서버 메모리 절약 |
| 보안 | 서버에서 세션 제어 | 토큰 만료시간으로 제어 |

---

## 2. JWT 기반 인증 시스템

### 2.1 JWT 구조

```
JWT = Header.Payload.Signature

예시:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNjQwOTk1MjAwfQ.
dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
```

**Header (헤더)**
```json
{
  "alg": "HS256",    // 서명 알고리즘
  "typ": "JWT"       // 토큰 타입
}
```

**Payload (페이로드)**
```json
{
  "sub": "testuser",        // 사용자 ID
  "role": "USER",           // 사용자 역할
  "type": "access",         // 토큰 타입
  "iat": 1640908800,        // 발급 시간
  "exp": 1640995200         // 만료 시간
}
```

**Signature (서명)**
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret_key
)
```

### 2.2 Access Token vs Refresh Token

```
Access Token:
- 짧은 만료시간 (1시간)
- API 호출에 사용
- 보안 위험 최소화

Refresh Token:
- 긴 만료시간 (7일)
- Access Token 갱신에만 사용
- 보안 저장소에 보관
```

---

## 3. 구현 아키텍처

### 3.1 전체 시스템 구조

```
                     ┌─────────────────┐
                     │   Client App    │
                     └─────────────────┘
                              │
                    HTTP Request + JWT Token
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (Port: 8080)                 │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │  JWT Filter     │ →  │  Route to Services              │ │
│  │  - 토큰 검증    │    │  - member-service: /api/members │ │
│  │  - 사용자 추출  │    │  - board-service: /api/boards   │ │
│  │  - 헤더 전달    │    └─────────────────────────────────┘ │
│  └─────────────────┘                                        │
└─────────────────────────────────────────────────────────────┘
              │                                 │
    X-USER-ID │                                 │ X-USER-ID
    X-USER-ROLE                                 │ X-USER-ROLE
              │                                 │
              ↓                                 ↓
┌─────────────────────┐              ┌─────────────────────┐
│  Member Service     │              │   Board Service     │
│  (Port: 8081)       │   API Call   │   (Port: 8082)      │
│                     │ ────────────→│                     │
│  - JWT 발급         │              │   - 게시글 CRUD     │
│  - 사용자 관리      │              │   - 사용자 ID 활용  │
│  - Spring Security  │              │                     │
└─────────────────────┘              └─────────────────────┘
              │
              │ 서비스 등록/발견
              ↓
┌─────────────────────┐
│   Eureka Server     │
│   (Port: 8761)      │
│                     │
│   - 서비스 레지스트리│
│   - 헬스체크        │
└─────────────────────┘
```

### 3.2 인증 및 인가 흐름

```
1. 로그인 요청 흐름:
   Client → API Gateway → Member Service → JWT 발급

2. API 호출 흐름:
   Client(+JWT) → API Gateway(토큰검증) → Backend Service(사용자 정보)

3. 세부 흐름:
   ┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
   │ Client  │    │ API Gateway │    │ JWT Filter  │    │   Service   │
   └─────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │               │                   │                 │
        │ POST /login   │                   │                 │
        ├──────────────→│                   │                 │
        │               │ (토큰검증 안함)   │                 │
        │               ├──────────────────────────────────→│
        │               │                   │    JWT 발급     │
        │               │←──────────────────────────────────┤
        │ JWT Response  │                   │                 │
        │←──────────────┤                   │                 │
        │               │                   │                 │
        │ GET /api/boards + Authorization: Bearer <JWT>        │
        ├──────────────→│                   │                 │
        │               │ JWT 토큰 검증     │                 │
        │               ├──────────────────→│                 │
        │               │ 사용자 정보 추출  │                 │
        │               │←──────────────────┤                 │
        │               │ + X-USER-ID 헤더  │                 │
        │               ├──────────────────────────────────→│
        │               │                   │ 비즈니스 로직   │
        │               │←──────────────────────────────────┤
        │ Response      │                   │                 │
        │←──────────────┤                   │                 │
```

---

## 4. 단계별 구현 가이드

### 4.1 1단계: 의존성 추가

**member-service/build.gradle**
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    // JWT 및 Security 의존성 추가
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.6'
}
```

**api-gateway/build.gradle**
```gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    // JWT 검증을 위한 의존성 추가
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.6'
}
```

### 4.2 2단계: JWT 발급 구현 (Member Service)

**1) JWT 설정 (application.yml)**
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890  # 32자 이상
  access-token-expiration: 3600000   # 1시간
  refresh-token-expiration: 604800000 # 7일
```

**2) Security 설정**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/members/login", "/members/register", "/members/health").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

**3) JWT 유틸리티 클래스**
```java
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    // Access Token 생성
    public String generateAccessToken(String memberId, String role) {
        return Jwts.builder()
                .setSubject(memberId)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

**4) 로그인 API**
```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    // 사용자 인증 (실제로는 DB 조회)
    if ("testuser".equals(request.getMemberId()) && "password123".equals(request.getPassword())) {
        String accessToken = jwtUtil.generateAccessToken(request.getMemberId(), "USER");
        String refreshToken = jwtUtil.generateRefreshToken(request.getMemberId());
        
        LoginResponse response = new LoginResponse(
            request.getMemberId(), accessToken, refreshToken, "USER");
        return ResponseEntity.ok(response);
    }
    return ResponseEntity.status(401).build();
}
```

### 4.3 3단계: JWT 검증 필터 구현 (API Gateway)

**1) JWT 검증 필터**
```java
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 공개 경로 체크
            if (isPublicPath(request.getURI().getPath())) {
                return chain.filter(exchange);
            }
            
            // JWT 토큰 추출
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            String token = authHeader.substring(7);
            
            // JWT 검증
            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
            
            // 사용자 정보 추출 및 헤더 추가
            String memberId = jwtUtil.getMemberIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-USER-ID", memberId)
                    .header("X-USER-ROLE", role)
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }
}
```

**2) 라우팅 설정 (application.yml)**
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 로그인 경로 (인증 불필요)
        - id: member-service-login
          uri: lb://member-service
          predicates:
            - Path=/api/members/login
          filters:
            - StripPrefix=1
        
        # 기타 회원 서비스 (인증 필요)
        - id: member-service
          uri: lb://member-service
          predicates:
            - Path=/api/members/**
          filters:
            - StripPrefix=1
            - name: JwtAuthenticationFilter
        
        # 게시판 서비스 (인증 필요)
        - id: board-service
          uri: lb://board-service
          predicates:
            - Path=/api/boards/**
          filters:
            - StripPrefix=1
            - name: JwtAuthenticationFilter
```

### 4.4 4단계: 다른 서비스에서 사용자 정보 활용

**Board Service 컨트롤러 예시**
```java
@RestController
@RequestMapping("/boards")
public class BoardController {
    
    @PostMapping
    public String createBoard(@RequestBody String boardData,
                             @RequestHeader("X-USER-ID") String userId) {
        // JWT에서 추출된 사용자 ID 활용
        return "사용자 " + userId + "가 게시글을 작성했습니다.";
    }
    
    @GetMapping
    public String getAllBoards(@RequestHeader(value = "X-USER-ID", required = false) String userId) {
        if (userId != null) {
            return "사용자 " + userId + "가 게시글 목록을 조회했습니다.";
        }
        return "게시글 목록입니다.";
    }
}
```

---

## 5. 보안 고려사항

### 5.1 JWT 보안 모범 사례

**1) 토큰 저장 방식**
```javascript
// ❌ 잘못된 방법: localStorage 사용 (XSS 취약)
localStorage.setItem('token', jwt);

// ✅ 권장 방법: HttpOnly 쿠키 사용
// Set-Cookie: token=jwt; HttpOnly; Secure; SameSite=Strict
```

**2) 토큰 만료시간 설정**
```yaml
jwt:
  access-token-expiration: 900000    # 15분 (짧게)
  refresh-token-expiration: 604800000 # 7일
```

**3) 비밀키 관리**
```yaml
# ❌ 하드코딩 금지
jwt:
  secret: hardcoded-secret

# ✅ 환경변수 사용
jwt:
  secret: ${JWT_SECRET:default-secret-for-dev}
```

### 5.2 API Gateway 보안 강화

**1) CORS 설정**
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: 
              - "https://your-frontend-domain.com"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
            allowedHeaders: "*"
            allowCredentials: true
```

**2) Rate Limiting**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: member-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

### 5.3 서비스별 권한 관리

**역할 기반 접근 제어 (RBAC)**
```java
@PostMapping("/admin/users")
public String adminAPI(@RequestHeader("X-USER-ROLE") String role) {
    if (!"ADMIN".equals(role)) {
        throw new AccessDeniedException("관리자 권한이 필요합니다.");
    }
    // 관리자만 접근 가능한 로직
}
```

---

## 6. 테스트 및 검증

### 6.1 단위 테스트

**JWT 유틸리티 테스트**
```java
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
    
    @Test
    void 토큰_생성_및_검증_테스트() {
        // Given
        String memberId = "testuser";
        String role = "USER";
        
        // When
        String token = jwtUtil.generateAccessToken(memberId, role);
        
        // Then
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(memberId, jwtUtil.getMemberIdFromToken(token));
        assertEquals(role, jwtUtil.getRoleFromToken(token));
    }
}
```

### 6.2 통합 테스트

**API 테스트 시나리오**

**1) 로그인 테스트**
```bash
# 1. 로그인하여 JWT 토큰 획득
curl -X POST http://localhost:8080/api/members/login \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "testuser",
    "password": "password123"
  }'

# 응답:
{
  "memberId": "testuser",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "USER"
}
```

**2) 인증된 API 호출**
```bash
# 2. JWT 토큰으로 보호된 API 호출
curl -X GET http://localhost:8080/api/boards \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 응답:
"Board Service: 사용자 testuser (USER)가 모든 게시글 목록을 조회했습니다."
```

**3) 인증 실패 테스트**
```bash
# 3. JWT 없이 API 호출 (401 에러 확인)
curl -X GET http://localhost:8080/api/boards

# 응답: 401 Unauthorized

# 4. 유효하지 않은 JWT로 API 호출
curl -X GET http://localhost:8080/api/boards \
  -H "Authorization: Bearer invalid.jwt.token"

# 응답: 401 Unauthorized
```

### 6.3 성능 테스트

**JWT 검증 성능 측정**
```java
@Test
void JWT_검증_성능_테스트() {
    String token = jwtUtil.generateAccessToken("testuser", "USER");
    
    long startTime = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
        jwtUtil.validateToken(token);
    }
    long endTime = System.nanoTime();
    
    long duration = (endTime - startTime) / 1_000_000; // ms
    System.out.println("10,000번 검증 소요시간: " + duration + "ms");
}
```

---

## 💡 핵심 포인트 정리

### ✅ MSA JWT 보안의 핵심
1. **API Gateway 중앙 집중식 인증**: 모든 요청은 API Gateway를 통과
2. **Stateless JWT 토큰**: 서버 확장성을 위한 토큰 기반 인증
3. **서비스별 사용자 컨텍스트**: 각 서비스는 헤더로 사용자 정보 전달받음
4. **보안 계층화**: Gateway 필터링 + 서비스별 권한 체크

### ⚠️ 주의사항
1. **경로 매칭**: API Gateway 라우팅과 Spring Security 경로 일치 확인
2. **토큰 만료시간**: 보안과 사용성의 균형 고려
3. **비밀키 관리**: 환경변수 또는 보안 저장소 사용
4. **에러 처리**: 명확한 인증/인가 에러 메시지 제공

### 🚀 확장 고려사항
- **Refresh Token 자동 갱신** 구현
- **다중 서비스 간 권한 전파** 메커니즘
- **JWT 블랙리스트** 관리 (로그아웃 시)
- **분산 세션 캐시** (Redis) 활용 검토

---

## 📋 체크리스트

### 구현 완료 확인
- [x] JWT 의존성 추가 완료
- [x] Member Service JWT 발급 구현
- [x] API Gateway JWT 검증 필터 구현  
- [x] 다른 서비스에서 사용자 정보 활용 구현
- [x] 로그인 API 정상 동작 확인
- [ ] 보호된 API JWT 인증 확인
- [ ] 인증 실패 시 401 응답 확인

### 보안 점검
- [ ] 비밀키 환경변수 설정
- [x] 적절한 토큰 만료시간 설정
- [ ] CORS 정책 적용
- [ ] Rate Limiting 적용 (선택사항)
- [ ] HTTPS 적용 (운영환경)

이 교안을 통해 MSA 환경에서의 JWT 기반 보안 시스템을 체계적으로 학습하고 구현할 수 있습니다.