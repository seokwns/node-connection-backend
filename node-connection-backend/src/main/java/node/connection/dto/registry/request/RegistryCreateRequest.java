package node.connection.dto.registry;

public record RegistryCreateRequest(
        String phoneNumber,
        String name,
        String birthDate,
        String email,
        String password,

) {
}
