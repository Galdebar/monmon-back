package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private String userEmail;
    private String userPassword;
    private Set<String> linkedUsers = new HashSet<>();
}
