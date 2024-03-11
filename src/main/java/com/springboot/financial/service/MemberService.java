package com.springboot.financial.service;

import com.springboot.financial.dto.Auth.SignIn;
import com.springboot.financial.dto.Auth.SignUp;
import com.springboot.financial.entity.MemberEntity;
import com.springboot.financial.exception.impl.AlreadyExistUserException;
import com.springboot.financial.exception.impl.PasswordDoesNotMatchException;
import com.springboot.financial.exception.impl.UsernameNotFoundException;
import com.springboot.financial.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        log.info("Loading user by username: {}", username);
        UserDetails userDetails = this.memberRepository.findByUsername(username)
                .orElseThrow(UsernameNotFoundException::new);
        log.info("User loaded successfully for username: {}", username);
        return userDetails;
    }

    @Transactional
    public SignUp.Response register(SignUp.Request request) {
        log.info("Attempting to register new user with username: {}", request.getUsername());
        boolean exists = this.memberRepository.existsByUsername(request.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        MemberEntity savedUser = this.memberRepository.save(
                MemberEntity.builder()
                        .username(request.getUsername())
                        .password(this.passwordEncoder.encode(request.getPassword()))
                        .roles(request.getRoles())
                        .build()
        );
        log.info("User registered successfully with username: {}", request.getUsername());
        return SignUp.Response.fromEntity(savedUser);
    }

    public SignIn.Response authenticate(SignIn.Request request) {
        log.info("Attempting to authenticate user with username: {}", request.getUsername());
        MemberEntity user = this.memberRepository.findByUsername(request.getUsername())
                .orElseThrow(UsernameNotFoundException::new);
        if (!this.passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordDoesNotMatchException();
        }

        log.info("User authenticated successfully for username: {}", user.getUsername());
        return SignIn.Response.fromEntity(user);
    }
}
