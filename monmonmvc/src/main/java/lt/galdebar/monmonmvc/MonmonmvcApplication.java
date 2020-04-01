package lt.galdebar.monmonmvc;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.function.Function;

@SpringBootApplication(scanBasePackages = "lt.galdebar")
@Log4j2
public class MonmonmvcApplication {


    public static void main(String[] args) {
        SpringApplication.run(MonmonmvcApplication.class, args);
        log.info("MonMon app launched.");
    }


}
