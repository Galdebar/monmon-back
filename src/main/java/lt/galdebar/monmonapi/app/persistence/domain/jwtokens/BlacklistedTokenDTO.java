package lt.galdebar.monmonapi.app.persistence.domain.jwtokens;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlacklistedTokenDTO {
    private Long id;
    private String token;
    private LocalDateTime dateAdded;
}
