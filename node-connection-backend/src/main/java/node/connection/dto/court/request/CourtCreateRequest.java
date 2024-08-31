package node.connection.dto.court.request;

import java.util.List;

public record CourtCreateRequest(
        String court,
        String support,
        String office,
        String phoneNumber,
        String address,
        String faxNumber,
        List<String> jurisdictions,
        String walletPassword
) {
}
