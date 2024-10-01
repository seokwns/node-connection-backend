package node.connection.data;

import java.time.LocalDateTime;

public record IssuanceData(
        String txId,
        String issuerName,
        String issuerDataHash,
        RegistryDocument registryDocument,
        LocalDateTime issuanceDate,
        LocalDateTime expirationDate
) {
}
