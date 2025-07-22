package com.example.boardservice.controller;

import com.example.boardservice.client.MemberServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * MSA 핵심 개념 7: 서비스 간 통신 예제
 * 
 * Board Service는 게시글 관리 기능을 제공하지만,
 * 게시글 작성 시 회원 정보가 필요할 때 Member Service를 호출합니다.
 * 
 * 이는 MSA의 핵심 원칙인 "단일 책임 원칙"을 지키면서도
 * 서비스 간 협력을 통해 복합적인 기능을 제공하는 예시입니다.
 */
@RestController
@RequestMapping("/boards")
public class BoardController {

    /**
     * MSA 핵심: 의존성 주입을 통한 서비스 간 통신
     * Spring이 MemberServiceClient 인터페이스의 구현체를 자동으로 생성하여 주입
     */
    @Autowired
    private MemberServiceClient memberServiceClient;
    
    @Value("${server.port}")
    private String serverPort;

    @GetMapping
    public String getAllBoards() {
        return "Board Service (Port: " + serverPort + "): 모든 게시글 목록을 조회했습니다.";
    }

    @GetMapping("/{id}")
    public String getBoard(@PathVariable String id) {
        return "Board Service (Port: " + serverPort + "): 게시글 ID " + id + "를 조회했습니다.";
    }

    /**
     * MSA 핵심 예시: 서비스 간 통신
     * 
     * 게시글 작성 시 회원 정보가 필요한 상황에서
     * Member Service를 호출하여 데이터를 가져옵니다.
     * 
     * 동작 순서:
     * 1. 클라이언트가 API Gateway에 요청
     * 2. API Gateway가 Board Service로 라우팅
     * 3. Board Service가 Member Service를 호출 (이 단계)
     * 4. Member Service가 회원 정보 반환
     * 5. Board Service가 게시글 생성 및 결과 반환
     */
    @PostMapping
    public String createBoard(@RequestBody String boardData, @RequestParam String memberId) {
        // MSA 핵심: 다른 서비스 호출을 통한 데이터 연동
        String memberInfo = memberServiceClient.getMember(memberId);
        return "Board Service: 새 게시글이 작성되었습니다. 작성자 정보: " + memberInfo + ", 게시글 데이터: " + boardData;
    }

    @PutMapping("/{id}")
    public String updateBoard(@PathVariable String id, @RequestBody String boardData) {
        return "Board Service: 게시글 ID " + id + "가 수정되었습니다. 데이터: " + boardData;
    }

    @DeleteMapping("/{id}")
    public String deleteBoard(@PathVariable String id) {
        return "Board Service: 게시글 ID " + id + "가 삭제되었습니다.";
    }

    @GetMapping("/health")
    public String health() {
        return "Board Service is running on port " + serverPort;
    }
}