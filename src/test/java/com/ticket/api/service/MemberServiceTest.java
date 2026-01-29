package com.ticket.api.service;

import com.ticket.api.dto.SignUpRequest;
import com.ticket.api.entity.Member;
import com.ticket.api.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원가입_성공_테스트")
    void signUp_success() {
        // given
        SignUpRequest request = new SignUpRequest();
        String rawPassword = "password1234";
        String encodedPassword = "encodedPassword1234";

        given(memberRepository.findByEmail(any())).willReturn(Optional.empty());

        given(passwordEncoder.encode(any())).willReturn(encodedPassword);

        Member savedMember = Member.builder()
                .email("test@test.com")
                .password(encodedPassword)
                .name("tester")
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(savedMember, "id", 1L);

        given(memberRepository.save(any())).willReturn(savedMember);

        // when (실행)
        org.springframework.test.util.ReflectionTestUtils.setField(request, "email", "test@test.com");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", rawPassword);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "tester");

        Long savedId = memberService.signUp(request);

        // then (검증)
        assertEquals(1L, savedId);

        // verify: save 호출
        verify(memberRepository).save(any());
        // verify: 비밀번호 암호화 호출
        verify(passwordEncoder).encode(any());
    }

    @Test
    @DisplayName("중복_이메일_가입_실패_테스트")
    void signUp_fail_duplicate_email() {
        // given
        SignUpRequest request = new SignUpRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "email", "exist@test.com");

        // 이메일 중복
        given(memberRepository.findByEmail(any())).willReturn(Optional.of(Member.builder().build()));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> memberService.signUp(request));
    }
}