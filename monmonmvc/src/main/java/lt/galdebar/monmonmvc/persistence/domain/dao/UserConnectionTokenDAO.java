package lt.galdebar.monmonmvc.persistence.domain.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "user_sync_tokens")
public class UserConnectionTokenDAO {
    @Indexed
    private String token;
    @DBRef(lazy = true)
    @NotNull
    private UserDAO userA;
    @DBRef(lazy = true)
    @NotNull
    private UserDAO userB;

    private Date expiryDate;
}
