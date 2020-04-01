package lt.galdebar.monmonmvc.persistence.domain.dao.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "email_change_tokens")
@ToString
public class UserEmailChangeTokenDAO {
    private UserDAO user;
    private String newEmail;
    private String token;
    private Date expiryDate;

}
