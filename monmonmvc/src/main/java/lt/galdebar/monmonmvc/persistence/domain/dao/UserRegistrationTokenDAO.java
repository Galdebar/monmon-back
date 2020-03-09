package lt.galdebar.monmonmvc.persistence.domain.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "registration_tokens")
public class UserRegistrationTokenDAO {

    @Id
    private String id;
    @Indexed
    private String token;
    @DBRef(lazy = true)
    @NotNull
    private UserDAO user;

    private Date expiryDate;



}
