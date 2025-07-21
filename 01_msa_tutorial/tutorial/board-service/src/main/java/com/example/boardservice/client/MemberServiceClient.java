package com.example.boardservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * MSA 핵심 개념 6: Feign Client (서비스 간 통신)
 * 
 * Feign Client는 다른 마이크로서비스를 호출하기 위한 선언적 HTTP 클라이언트입니다.
 * 
 * 동작 원리:
 * 1. @FeignClient 어노테이션으로 대상 서비스 지정
 * 2. 인터페이스에 HTTP 메서드와 경로 정의
 * 3. Spring Cloud가 런타임에 실제 구현체 생성
 * 4. Eureka를 통해 대상 서비스의 위치 자동 발견
 * 5. 로드밸런싱과 서킷 브레이커 자동 적용
 * 
 * 이점:
 * - 복잡한 HTTP 호출 코드를 간단한 인터페이스로 추상화
 * - 서비스 디스커버리와 로드밸런싱 자동 처리
 * - 다른 서비스 API 변경 시 인터페이스만 수정하면 됨
 */
@FeignClient(name = "member-service")  // Eureka에 등록된 'member-service'라는 이름의 서비스를 호출
public interface MemberServiceClient {

    /**
     * Member Service의 회원 조회 API를 호출
     * 
     * 예: getMember("1") 호출 시
     * 1. Eureka에서 member-service 인스턴스 위치 조회
     * 2. HTTP GET http://member-service-ip:port/members/1 요청
     * 3. 응답 데이터를 String으로 반환
     */
    @GetMapping("/members/{id}")
    String getMember(@PathVariable("id") String id);
}