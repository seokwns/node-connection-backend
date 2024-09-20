package node.connection.dto.user.request;

public record JoinDTO (

    String username,
    String phoneNumber,
    String email,
    String courtCode
) {}
