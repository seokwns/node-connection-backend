package node.connection.dto.registry.request;

import node.connection.dto.registry.RegistryDocumentDto;

public record RegistryCreateRequest(
        String phoneNumber,
        String name,
        String birthDate,
        String email,
        String password,
        RegistryDocumentDto documentDto
) {
}
