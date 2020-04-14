package lt.galdebar.monmonmvc.context.security.exceptions;

import io.jsonwebtoken.JwtException;

public class InvalidJwtAuthenticationException extends JwtException {
    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }
}
