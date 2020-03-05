package lt.galdebar.monmonmvc.persistence.domain.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class UserDAO {
    @Id
    private String id;
    private String userEmail;
    private String userPassword;

}


