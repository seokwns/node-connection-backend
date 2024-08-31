package node.connection.dto.court.request;

public record AddCourtMemberRequest(
        String court,
        String support,
        String office,
        String memberId
) {
}
