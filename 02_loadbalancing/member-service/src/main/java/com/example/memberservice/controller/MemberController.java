package com.example.memberservice.controller;

import org.springframework.web.bind.annotation.*;

/**
 * MSA 핵심 개념 4: REST API 설계
 * 
 * 멤버 서비스의 REST API 엔드포인트를 정의합니다.
 * MSA에서 서비스 간 통신은 주로 HTTP REST API를 사용합니다.
 * 
 * REST API 설계 원칙:
 * - GET: 데이터 조회 (비건멱성)
 * - POST: 데이터 생성
 * - PUT: 데이터 전체 수정
 * - DELETE: 데이터 삭제
 */
@RestController  // 이 클래스의 모든 메서드는 JSON 응답을 반환
@RequestMapping("/members")  // 이 컨트롤러의 모든 API는 /members 경로로 시작
public class MemberController {

    /**
     * 회원 정보 조회 API
     * 예: GET /members/1 → ID 1번 회원 정보 반환
     */
    @GetMapping("/{id}")
    public String getMember(@PathVariable String id) {
        // 실제 프로덕션에서는 데이터베이스에서 회원 정보를 조회
        return "Member Service: 회원 ID " + id + "의 정보를 조회했습니다.";
    }

    /**
     * 새 회원 가입 API
     * 예: POST /members 요청 바디에 회원 정보 전송
     */
    @PostMapping
    public String createMember(@RequestBody String memberData) {
        // 실제 프로덕션에서는 데이터베이스에 회원 정보 저장
        return "Member Service: 새로운 회원이 가입되었습니다. 데이터: " + memberData;
    }

    /**
     * 회원 정보 수정 API
     * 예: PUT /members/1 → ID 1번 회원 정보 수정
     */
    @PutMapping("/{id}")
    public String updateMember(@PathVariable String id, @RequestBody String memberData) {
        return "Member Service: 회원 ID " + id + "의 정보가 수정되었습니다. 데이터: " + memberData;
    }

    /**
     * 회원 탈퇴 API
     * 예: DELETE /members/1 → ID 1번 회원 삭제
     */
    @DeleteMapping("/{id}")
    public String deleteMember(@PathVariable String id) {
        return "Member Service: 회원 ID " + id + "가 탈퇴되었습니다.";
    }

    /**
     * 헬스체크 API - MSA에서 중요한 개념
     * 
     * MSA 헬스체크 동작 방식:
     * 1. Eureka Server가 30초마다 이 엔드포인트를 자동 호출하여 서비스 상태 확인
     * 2. API Gateway가 로드밸런싱 시 살아있는 인스턴스만 선택
     * 3. 90초 동안 응답 없으면 Eureka에서 해당 인스턴스 제거
     * 4. Spring Cloud가 자동으로 헬스체크를 관리하므로 별도 설정 불필요
     * 
     * 참고: application.yml에서 lease-renewal-interval-in-seconds로 주기 조정 가능
     */
    @GetMapping("/health")
    public String health() {
        return "Member Service is running on port 8081";
    }
}