package lt.galdebar.monmonapi.app.services.shoppinglists.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ListNameEmpty extends RuntimeException {
    private static final String MESSAGE = "List name cannot be empty";
    public ListNameEmpty() {
        super(MESSAGE);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
