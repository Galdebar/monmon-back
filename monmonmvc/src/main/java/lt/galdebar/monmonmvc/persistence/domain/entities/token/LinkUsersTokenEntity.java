package lt.galdebar.monmonmvc.persistence.domain.entities.token;

import lombok.*;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "user_sync_tokens")
@Data
public class LinkUsersTokenEntity {
    @Id
    private String id;
    @Indexed(unique = true)
    private String token;
    @DBRef(lazy = true)
    @NotNull
    private UserEntity userA;
    @DBRef(lazy = true)
    @NotNull
    private UserEntity userB;

    private Date expiryDate;
}
