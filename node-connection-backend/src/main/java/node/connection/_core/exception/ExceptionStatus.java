package node.connection._core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ExceptionStatus {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1000, "서버에서 알 수 없는 에러가 발생했습니다."),
    INVALID_METHOD_ARGUMENTS_ERROR(HttpStatus.BAD_REQUEST, 1001, ""),
    WALLET_CREATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2000, "지갑 생성 중 에러가 발생했습니다."),
    WALLET_OPEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2001, "지갑 조회 중 에러가 발생했습니다."),
    ;

    private final HttpStatus status;
    private final int code;
    private final String message;
}
