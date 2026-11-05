package com.example.CoolShopWebsite.dto.response;

import com.example.CoolShopWebsite.model.User;
import com.example.CoolShopWebsite.model.enums.UserRole;
import com.example.CoolShopWebsite.model.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String fullname;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId()).fullname(u.getFullname()).email(u.getEmail())
                .role(u.getRole()).status(u.getStatus())
                .createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt())
                .build();
    }
}
