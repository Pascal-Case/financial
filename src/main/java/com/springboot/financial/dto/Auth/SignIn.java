package com.springboot.financial.dto.Auth;

import com.springboot.financial.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SignIn {

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String username;
        private List<String> roles;
        private String token;


        public static Response fromEntity(MemberEntity memberEntity) {
            return Response.builder()
                    .username(memberEntity.getUsername())
                    .roles(memberEntity.getRoles())
                    .build();
        }
    }

}
