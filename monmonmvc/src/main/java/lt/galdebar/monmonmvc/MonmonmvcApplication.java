package lt.galdebar.monmonmvc;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.function.Function;

@SpringBootApplication
public class MonmonmvcApplication {


    public static void main(String[] args) {
        SpringApplication.run(MonmonmvcApplication.class, args);

        System.out.println("app runs!");
    }


}
