package lt.galdebar.monmonmvc.persistence.domain.dao;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "users")
@ToString
public class UserDAO {
    @Id
    private String id;
    private String userEmail;
    private String userPassword;
    @ToString.Exclude
    private Set<String> linkedUsers = new HashSet<>();
    private boolean isValidated;

}


