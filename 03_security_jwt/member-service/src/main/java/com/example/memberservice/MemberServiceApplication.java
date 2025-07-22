package com.example.memberservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MSA 핵심 개념 3: 마이크로서비스 (Member Service)
 * 
 * 각 마이크로서비스는 특정 비즈니스 도메인에 대한 책임을 가집니다.
 * 
 * Member Service의 책임:
 * - 회원 정보 관리 (CRUD)
 * - 회원 인증/인가
 * - 회원 프로필 관리
 * 
 * 마이크로서비스의 특징:
 * 1. 단일 책임 원칙: 하나의 서비스는 하나의 비즈니스 영역에만 집중
 * 2. 데이터 독립성: 각 서비스는 자신만의 데이터베이스를 소유
 * 3. 독립 배포: 다른 서비스와 무관하게 배포 가능
 * 4. 기술 스택 자유: 서비스별로 다른 언어/프레임워크 사용 가능
 */
@SpringBootApplication
@EnableDiscoveryClient  // 이 서비스를 Eureka Server에 등록하고 다른 서비스를 발견할 수 있게 하는 어노테이션
public class MemberServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberServiceApplication.class, args);
    }

}
