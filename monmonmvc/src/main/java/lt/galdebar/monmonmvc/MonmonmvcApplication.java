package lt.galdebar.monmonmvc;

import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.function.Function;

@SpringBootApplication
public class MonmonmvcApplication {
	@Autowired
	private static PasswordEncoder encoder;

    public static void main(String[] args) {
        SpringApplication.run(MonmonmvcApplication.class, args);

        System.out.println("app runs!");

        createTempUsers();


    }

    private static void createTempUsers() {
        String userName = "TestGuy";
        String password = "letmein";
		UserDTO userDTO = new UserDTO(userName,password);
    }

}
