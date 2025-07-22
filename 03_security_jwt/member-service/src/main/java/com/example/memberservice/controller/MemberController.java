package com.example.memberservice.controller;

import com.example.memberservice.dto.LoginRequest;
import com.example.memberservice.dto.LoginResponse;
import com.example.memberservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 회원 정보 조회 API
     * 예: GET /members/1 → ID 1번 회원 정보 반환
     */
    @GetMapping("/{id}")
    public String getMember(@PathVariable String id,
                           @RequestHeader(value = "X-USER-ID", required = false) String currentUserId) {
        // 실제 프로덕션에서는 데이터베이스에서 회원 정보를 조회
        if (currentUserId != null) {
            return "Member Service: 사용자 " + currentUserId + "가 회원 ID " + id + "의 정보를 조회했습니다.";
        }
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
    public String updateMember(@PathVariable String id, 
                              @RequestBody String memberData,
                              @RequestHeader("X-USER-ID") String currentUserId) {
        // 실제로는 본인 정보만 수정 가능하도록 권한 검증 필요
        return "Member Service: 사용자 " + currentUserId + "가 회원 ID " + id + "의 정보를 수정했습니다. 데이터: " + memberData;
    }

    /**
     * 회원 탈퇴 API
     * 예: DELETE /members/1 → ID 1번 회원 삭제
     */
    @DeleteMapping("/{id}")
    public String deleteMember(@PathVariable String id,
                              @RequestHeader("X-USER-ID") String currentUserId) {
        // 실제로는 본인 계정만 삭제 가능하도록 권한 검증 필요
        return "Member Service: 사용자 " + currentUserId + "가 회원 ID " + id + " 계정을 탈퇴 처리했습니다.";
    }

    /**
     * 로그인 API - JWT 토큰 발급
     * 사용자 인증 후 Access Token과 Refresh Token을 발급합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // 실제 프로덕션에서는 데이터베이스에서 사용자 정보를 조회하여 검증
        // 여기서는 데모용으로 간단한 하드코딩된 검증을 사용
        String memberId = loginRequest.getMemberId();
        String password = loginRequest.getPassword();
        
        // 간단한 테스트용 계정 검증 (실제로는 DB에서 조회)
        if ("testuser".equals(memberId) && "password123".equals(password)) {
            String role = "USER"; // 실제로는 DB에서 조회된 사용자의 역할
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(memberId, role);
            String refreshToken = jwtUtil.generateRefreshToken(memberId);
            
            LoginResponse response = new LoginResponse(memberId, accessToken, refreshToken, role);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).build(); // 인증 실패
        }
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