package com.example.CoolShopWebsite.dto.request;

import com.example.CoolShopWebsite.model.enums.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserRequest {
    @Size(max = 100) private String fullname;
    @Size(min = 6, max = 100) private String password;
    private UserStatus status;
}
