package com.example.boardservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * MSA 핵심 개념 5: 서비스 간 통신 (Service-to-Service Communication)
 * 
 * Board Service는 게시판 기능을 담당하지만, 회원 정보가 필요할 때
 * Member Service를 호출하여 데이터를 가져옵니다.
 * 
 * 서비스 간 통신 방식:
 * 1. 동기 통신: HTTP REST API 호출 (Feign Client 사용)
 * 2. 비동기 통신: 메시지 큐를 통한 이벤트 방식
 * 
 * 이 예제에서는 Feign Client를 사용한 동기 통신을 보여줍니다.
 */
@SpringBootApplication
@EnableDiscoveryClient  // Eureka에 서비스 등록 및 다른 서비스 발견
@EnableFeignClients     // Feign Client 활성화 - 다른 서비스를 쉽게 호출할 수 있게 해주는 라이브러리
public class BoardServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardServiceApplication.class, args);
    }

}
