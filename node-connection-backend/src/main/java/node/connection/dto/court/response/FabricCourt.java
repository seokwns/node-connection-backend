package node.connection.dto.court.response;

import java.util.List;

public record FabricCourt(
        String id,
        String court,
        String support,
        String office,
        String owner,
        List<String> members,
        List<FabricCourtRequest> requests,
        List<FabricCourtRequest> finalized
) {
}
