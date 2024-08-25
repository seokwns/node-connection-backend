package node.connection.data.wallet.metadata;

import lombok.Builder;
import lombok.Getter;
import node.connection.data.wallet.metadata.constant.MetadataType;

import java.time.LocalDateTime;

@Getter
public class DidMetadata {

    private MetadataType type;

    private String documentId;

    private LocalDateTime createdAt;

    private LocalDateTime expiredAt;

    @Builder
    public DidMetadata(MetadataType type, String documentId, LocalDateTime createdAt, LocalDateTime expiredAt) {
        this.type = type;
        this.documentId = documentId;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }
}
