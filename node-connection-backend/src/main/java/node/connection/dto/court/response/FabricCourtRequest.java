package node.connection.dto.court.response;

import com.fasterxml.jackson.databind.JsonNode;

public record FabricCourtRequest(
        String id,
        String documentId,
        String action,
        JsonNode payload,
        boolean finalized,
        String requestDate,
        String finalizeDate,
        String status,
        String errorMessage,
        String forwardedTo
) {
}
