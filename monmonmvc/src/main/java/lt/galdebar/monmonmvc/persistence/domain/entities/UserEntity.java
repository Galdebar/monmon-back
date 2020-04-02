package lt.galdebar.monmonmvc.persistence.domain.entities;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

//@NoArgsConstructor
//@Getter
//@Setter
//@ToString
@Data
@Document(collection = "users")
public class UserEntity {
    @Id
    private String id;
    private String userEmail;
    private String userPassword;
    @ToString.Exclude
    private Set<String> linkedUsers = new HashSet<>();
    private boolean isValidated;

}


