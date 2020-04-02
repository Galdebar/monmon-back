package lt.galdebar.monmonmvc.persistence.domain.entities.token;

import lombok.*;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Document(collection = "registration_tokens")
@Data
public class UserRegistrationTokenEntity {

    @Id
    private String id;
    @Indexed
    private String token;
    @DBRef(lazy = true)
    @NotNull
    private UserEntity user;

    private Date expiryDate;



}
