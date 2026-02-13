package com.ticket.api.controller;

import com.ticket.api.entity.Member;
import com.ticket.api.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MemberControllerTest {

    @Autowired
    private MemberController memberController;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void findMember_allows_self_email() throws Exception {
        String email = "user@test.com";
        memberRepository.save(Member.builder()
                .email(email)
                .name("User")
                .password("pw")
                .build());

        Principal principal = () -> email;

        ResponseEntity<?> response = memberController.findMember(email, principal);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void findMember_blocks_other_email() throws Exception {
        String email = "user@test.com";
        String otherEmail = "other@test.com";
        memberRepository.save(Member.builder()
                .email(otherEmail)
                .name("Other")
                .password("pw")
                .build());

        Principal principal = () -> email;

        ResponseEntity<?> response = memberController.findMember(otherEmail, principal);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
