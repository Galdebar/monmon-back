package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private String userEmail;
    private String userPassword;
}
