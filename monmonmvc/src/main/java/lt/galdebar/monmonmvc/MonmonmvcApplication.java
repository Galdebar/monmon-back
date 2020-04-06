package lt.galdebar.monmonmvc;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"lt.galdebar.monmonmvc", "lt.galdebar.monmon.categoriesparser"})
@EnableScheduling
@Log4j2
public class MonmonmvcApplication {


    public static void main(String[] args) {
        SpringApplication.run(MonmonmvcApplication.class, args);
        log.info("MonMon app launched.");
    }


}
