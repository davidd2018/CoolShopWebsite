package com.example.CoolShopWebsite.model;

import com.example.CoolShopWebsite.model.enums.UserRole;
import com.example.CoolShopWebsite.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tài khoản người dùng hệ thống.
 * Maps tới bảng: users
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_role",       columnList = "role"),
        @Index(name = "idx_users_status",     columnList = "status"),
        @Index(name = "idx_users_created_at", columnList = "created_at"),
        @Index(name = "idx_users_fullname",   columnList = "fullname")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * BCrypt hash của password. Không bao giờ lưu plaintext.
     * Không expose trường này ra Response DTO ở Controller layer.
     */
    @NotBlank
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20,
            columnDefinition = "ENUM('ADMIN','CUSTOMER') DEFAULT 'CUSTOMER'")
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20,
            columnDefinition = "ENUM('ACTIVE','INACTIVE','BANNED') DEFAULT 'ACTIVE'")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Relationships
    // ----------------------------------------------------------------

    /** Một User có nhiều Orders. FetchType.LAZY để tránh N+1 query. */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
