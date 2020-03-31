package lt.galdebar.monmonmvc.service.exceptions.shoppingitem;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.service.exceptions.CanSendResponse;

@RequiredArgsConstructor
public class ShoppingItemServiceException extends Throwable implements CanSendResponse {
    private static final String PARENT_MESSAGE = "Shopping Item Service Exception || ";
    private final String specificMessage;

    @Override
    public String getMessage() {
        return PARENT_MESSAGE + specificMessage;
    }

    @Override
    public String getResponseMessage() {
        return specificMessage;
    }
}
