package com.ticket.api.controller;

import com.ticket.api.dto.MemberResponse;
import com.ticket.api.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/{email}")
    public ResponseEntity<MemberResponse> findMember(@PathVariable String email) {
        return ResponseEntity.ok(memberService.findMember(email));
    }
}
