package changejar;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApiErrorCodes {
    USER_LOGGED_OUT("403", "User forced logged out", 403);
    private final String errorCode;
    private final String errorMessage;
    private final int expectedStatusCode;
}
