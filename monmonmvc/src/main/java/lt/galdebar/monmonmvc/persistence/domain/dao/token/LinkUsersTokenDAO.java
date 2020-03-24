package lt.galdebar.monmonmvc.persistence.domain.dao.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "user_sync_tokens")
public class LinkUsersTokenDAO {
    @Id
    private String id;
    @Indexed(unique = true)
    private String token;
    @DBRef(lazy = true)
    @NotNull
    private UserDAO userA;
    @DBRef(lazy = true)
    @NotNull
    private UserDAO userB;

    private Date expiryDate;
}
