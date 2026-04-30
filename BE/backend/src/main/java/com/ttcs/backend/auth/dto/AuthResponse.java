package com.ttcs.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private final Long id;
    private final String token;
    private final String tokenType;
    private final String email;
    private final String fullName;
    private final String role;
    private String message;
}
