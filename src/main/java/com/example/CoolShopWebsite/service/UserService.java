package com.example.CoolShopWebsite.service;

import com.example.CoolShopWebsite.dto.request.CreateUserRequest;
import com.example.CoolShopWebsite.dto.request.UpdateUserRequest;
import com.example.CoolShopWebsite.dto.response.UserResponse;
import com.example.CoolShopWebsite.exception.AppException;
import com.example.CoolShopWebsite.model.User;
import com.example.CoolShopWebsite.model.enums.UserRole;
import com.example.CoolShopWebsite.model.enums.UserStatus;
import com.example.CoolShopWebsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    public UserResponse findById(Long id) {
        return UserResponse.from(getOrThrow(id));
    }

    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw AppException.conflict("Email already exists");
        }
        User user = User.builder()
                .fullname(req.getFullname())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : UserRole.CUSTOMER)
                .status(req.getStatus() != null ? req.getStatus() : UserStatus.ACTIVE)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(Long id, UpdateUserRequest req) {
        User user = getOrThrow(id);
        if (req.getFullname() != null) user.setFullname(req.getFullname());
        if (req.getPassword() != null) user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getStatus() != null)   user.setStatus(req.getStatus());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse updateStatus(Long id, UserStatus status) {
        User user = getOrThrow(id);
        // Không cho khóa admin cuối cùng
        if (status != UserStatus.ACTIVE && user.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) throw AppException.badRequest("Cannot deactivate the last admin");
        }
        user.setStatus(status);
        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = getOrThrow(id);
        if (user.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) throw AppException.badRequest("Cannot delete the last admin");
        }
        if (!user.getOrders().isEmpty()) {
            throw AppException.conflict("Cannot delete user with existing orders");
        }
        userRepository.delete(user);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
    }
}
