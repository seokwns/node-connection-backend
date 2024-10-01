package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class IssuanceHistory {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    private UserAccount userAccount;

    @Column(nullable = false, unique = true)
    private String issuanceHash;

    @Column
    private String registryDocumentId;

    @Column
    private String address;

    @Column
    private LocalDateTime expiredAt;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Builder
    public IssuanceHistory(UserAccount userAccount, String issuanceHash, String registryDocumentId, String address, LocalDateTime expiredAt) {
        this.userAccount= userAccount;
        this.issuanceHash = issuanceHash;
        this.registryDocumentId = registryDocumentId;
        this.address = address;
        this.expiredAt = expiredAt;
    }

    @PrePersist
    protected void onCreated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected  void onUpdated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
