package node.connection.dto.registry.request;

import node.connection.dto.registry.RegistryDocumentDto;

public record RegistryCreateRequest(
        RegistryDocumentDto document
) {
}
