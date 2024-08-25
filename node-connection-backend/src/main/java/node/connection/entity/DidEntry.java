package node.connection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.entity.pk.DidEntryKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class DidEntry {
    @EmbeddedId
    private DidEntryKey key;

    @Column
    private String verKey;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public DidEntry(DidEntryKey key, String verKey, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.key = key;
        this.verKey = verKey;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }
}
