package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginAttemptDTO {
    private String userEmail;
    private String userPassword;
}
