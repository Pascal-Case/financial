package com.springboot.financial.web;

import com.springboot.financial.security.TokenProvider;
import com.springboot.financial.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.springboot.financial.model.Auth.SignIn;
import static com.springboot.financial.model.Auth.SignUp;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody SignUp request
    ) {
        log.info("Attempt to signup with username: {}", request.getUsername());
        var result = this.memberService.register(request);
        log.info("Signup successful for username: {}", request.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(
            @RequestBody SignIn request
    ) {
        log.info("Attempt to signin with username: {}", request.getUsername());
        var member = this.memberService.authenticate(request);
        var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());
        log.info("Signin successful, token generated for username: {}", request.getUsername());
        return ResponseEntity.ok(token);
    }

}
