package node.connection.data.wallet;

import java.time.LocalDateTime;

public record DidData(
        String documentId,
        LocalDateTime createdAt
) {
}
