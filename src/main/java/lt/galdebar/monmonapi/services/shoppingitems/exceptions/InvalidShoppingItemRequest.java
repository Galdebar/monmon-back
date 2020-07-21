package lt.galdebar.monmonapi.services.shoppingitems.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidShoppingItemRequest extends RuntimeException {
    public InvalidShoppingItemRequest(String message) {
        super(message);
    }
}
