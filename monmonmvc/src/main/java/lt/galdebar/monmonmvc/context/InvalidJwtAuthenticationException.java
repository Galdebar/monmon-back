package lt.galdebar.monmonmvc.context;

import io.jsonwebtoken.JwtException;

public class InvalidJwtAuthenticationException extends JwtException {
    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }
}
