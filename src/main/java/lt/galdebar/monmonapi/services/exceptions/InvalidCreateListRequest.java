package lt.galdebar.monmonapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidCreateListRequest extends RuntimeException {
    public InvalidCreateListRequest(String message){
        super(message);
    }
}
