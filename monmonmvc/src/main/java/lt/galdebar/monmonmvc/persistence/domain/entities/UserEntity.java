package lt.galdebar.monmonmvc.persistence.domain.entities;

import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;
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
    private boolean toBeDeleted;
    private Date deletionDate;

    public String getUserEmail() {
        return userEmail.toLowerCase();
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail.toLowerCase();
    }
}


