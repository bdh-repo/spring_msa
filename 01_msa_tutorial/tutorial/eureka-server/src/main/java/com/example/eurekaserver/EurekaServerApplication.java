package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * MSA 핵심 개념 1: Service Discovery (서비스 디스커버리)
 * 
 * Eureka Server는 MSA 환경에서 서비스 레지스트리 역할을 합니다.
 * 
 * 주요 역할:
 * 1. 서비스 등록: 각 마이크로서비스가 자신의 위치(IP, 포트)를 등록
 * 2. 서비스 발견: 다른 서비스들이 필요한 서비스의 위치를 조회
 * 3. 헬스체크: 등록된 서비스들의 상태를 주기적으로 확인
 * 4. 로드밸런싱: 동일한 서비스의 여러 인스턴스 중 적절한 인스턴스 선택
 * 
 * MSA에서 서비스들은 고정된 IP/포트가 아닌 서비스 이름으로 통신합니다.
 * 예: http://member-service/members/1 (IP 대신 서비스명 사용)
 */
@SpringBootApplication
@EnableEurekaServer  // 이 애플리케이션을 Eureka Server로 동작하게 하는 어노테이션
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
