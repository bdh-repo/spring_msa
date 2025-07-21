package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MSA 핵심 개념 2: API Gateway (단일 진입점)
 * 
 * API Gateway는 MSA의 단일 진입점(Single Entry Point) 역할을 합니다.
 * 
 * 주요 역할:
 * 1. 라우팅: 클라이언트 요청을 적절한 마이크로서비스로 전달
 * 2. 로드밸런싱: 동일한 서비스의 여러 인스턴스에 요청 분산
 * 3. 인증/인가: 보안 기능 중앙화
 * 4. 로깅/모니터링: 요청 추적 및 성능 모니터링
 * 5. 요청/응답 변환: 필요시 데이터 형식 변환
 * 
 * 이점:
 * - 클라이언트는 하나의 엔드포인트만 알면 됨
 * - 내부 서비스 구조 변경 시 클라이언트 영향 최소화
 * - 보안 정책을 한 곳에서 관리 가능
 */
@SpringBootApplication
@EnableDiscoveryClient  // Eureka 서버에서 다른 서비스들을 발견할 수 있게 하는 어노테이션
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
