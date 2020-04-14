package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EmailChangeRequestDTO {
    private String newEmail;
}
