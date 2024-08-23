package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.entity.pk.CourtKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class CourtWalletConfig {

    @Id
    private String id;

    @Column(nullable = false)
    private String password;

    @OneToOne
    private Court court;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public CourtWalletConfig(String id, String password, Court court, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.password = password;
        this.court = court;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static CourtWalletConfig of(String id, String password, Court court) {
        return CourtWalletConfig.builder()
                .id(id)
                .password(password)
                .court(court)
                .build();
    }
}
