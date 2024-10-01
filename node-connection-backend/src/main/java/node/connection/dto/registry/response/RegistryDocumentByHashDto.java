package node.connection.dto.registry.response;

import node.connection.data.RegistryDocument;

import java.time.LocalDateTime;

public record RegistryDocumentByHashDto(
        String txId,
        String issuer,
        LocalDateTime issuanceAt,
        LocalDateTime expiredAt,
        RegistryDocument hashedDocument,
        RegistryDocument latestDocument
) {
}
