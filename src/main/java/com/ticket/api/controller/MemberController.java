package com.ticket.api.controller;

import com.ticket.api.dto.MemberResponse;
import com.ticket.api.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "사용자 찾기 API", description = "이메일로 사용자 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/{email}")
    public ResponseEntity<MemberResponse> findMember(@PathVariable String email, Principal principal) {
        if (!email.equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(memberService.findMember(email));
    }
}
