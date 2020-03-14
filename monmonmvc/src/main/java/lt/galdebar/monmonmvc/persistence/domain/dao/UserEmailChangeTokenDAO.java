package lt.galdebar.monmonmvc.persistence.domain.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "email_change_tokens")
public class UserEmailChangeTokenDAO {
    private UserDAO user;
    private String newEmail;
    private String token;
    private Date expiryDate;

}
