package lt.galdebar.monmonapi.app.services.shoppinglists.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ListPasswordTooShort extends InvalidListRequest {
    private static final String MESSAGE = "Password must be longer than ";

    public ListPasswordTooShort(int minNumOfChars) {
        super(MESSAGE + minNumOfChars + " characters.");
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
