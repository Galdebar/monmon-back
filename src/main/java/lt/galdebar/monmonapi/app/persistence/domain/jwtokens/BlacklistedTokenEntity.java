package lt.galdebar.monmonapi.app.persistence.domain.jwtokens;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "blacklisted_token")
@Data
@Table(name = "blacklisted_tokens")
public class BlacklistedTokenEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition="TEXT")
    private String token;
    private LocalDateTime dateAdded;

    public BlacklistedTokenDTO getDTO(){
        BlacklistedTokenDTO dto = new BlacklistedTokenDTO();
        dto.setId(id);
        dto.setToken(token);
        dto.setDateAdded(dateAdded);
        return dto;
    }
}
