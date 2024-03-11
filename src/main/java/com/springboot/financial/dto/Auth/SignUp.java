package com.springboot.financial.dto.Auth;

import com.springboot.financial.entity.MemberEntity;
import lombok.*;

import java.util.List;

public class SignUp {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String username;
        private String password;
        private List<String> roles;

    }

    @Builder
    @Getter
    public static class Response {
        private String username;
        private List<String> roles;

        public static SignUp.Response fromEntity(MemberEntity memberEntity) {
            return Response.builder()
                    .username(memberEntity.getUsername())
                    .roles(memberEntity.getRoles())
                    .build();
        }
    }
}
