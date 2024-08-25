package node.connection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import node.connection.entity.constant.Role;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_account_tb")
public class UserAccount {
    @Id
    private String phoneNumber;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String residentNumber;

    @Column(nullable = false)
    private String password;

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive;
}
