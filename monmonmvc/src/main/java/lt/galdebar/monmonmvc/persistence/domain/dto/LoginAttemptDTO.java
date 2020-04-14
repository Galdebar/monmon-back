package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginAttemptDTO {
    private String userEmail;
    private String userPassword;
}
