package lt.galdebar.monmonmvc.persistence.domain.entities.token;

import lombok.*;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "email_change_tokens")
@Data
public class UserEmailChangeTokenEntity {
    private UserEntity user;
    private String newEmail;
    private String token;
    private Date expiryDate;

}
