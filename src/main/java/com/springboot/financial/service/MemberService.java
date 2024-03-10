package com.springboot.financial.service;

import com.springboot.financial.exception.impl.AlreadyExistUserException;
import com.springboot.financial.exception.impl.PasswordDoesNotMatchException;
import com.springboot.financial.exception.impl.UsernameNotFoundException;
import com.springboot.financial.model.Auth;
import com.springboot.financial.model.MemberEntity;
import com.springboot.financial.persist.MemberRepository;
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
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        log.info("Loading user by username: {}", username);
        UserDetails userDetails = this.memberRepository.findByUsername(username)
                .orElseThrow(UsernameNotFoundException::new);
        log.info("User loaded successfully for username: {}", username);
        return userDetails;
    }

    public MemberEntity register(Auth.SignUp member) {
        log.info("Attempting to register new user with username: {}", member.getUsername());
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        String encodedPassword = this.passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        MemberEntity savedMember = this.memberRepository.save(member.toEntity());
        log.info("User registered successfully with username: {}", member.getUsername());
        return savedMember;
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        log.info("Attempting to authenticate user with username: {}", member.getUsername());
        MemberEntity user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(UsernameNotFoundException::new);
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new PasswordDoesNotMatchException();
        }

        log.info("User authenticated successfully for username: {}", member.getUsername());
        return user;
    }
}
