package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthTokenDTO {
    private String userEmail;
    private String token;
}
