package lt.galdebar.monmonmvc.persistence.domain.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class UserDAO {
    @Id
    private String id;
    private String userEmail;
    private String userPassword;
    private Set<String> linkedUsers = new HashSet<>();
    private boolean isValidated;

}


