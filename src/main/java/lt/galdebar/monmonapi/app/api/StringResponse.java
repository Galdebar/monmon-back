package lt.galdebar.monmonapi.app.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StringResponse {
    private final String message;
}
