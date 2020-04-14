package lt.galdebar.monmonmvc.service.exceptions.login;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.service.exceptions.CanSendResponse;

@RequiredArgsConstructor
public class LoginException extends Throwable implements CanSendResponse {
    private static final String PARENT_MESSAGE = "Login Exception || ";
    private final String userEmail;
    private final String specificMessage;
    @Override
    public String getMessage() {
        return PARENT_MESSAGE + specificMessage + userEmail;
    }

    @Override
    public String getResponseMessage() {
        return specificMessage;
    }
}
