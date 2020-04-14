package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PasswordChangeRequestDTO {
    private String userEmail;
    private String oldPassword;
    private String newPassword;
}