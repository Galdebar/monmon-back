package lt.galdebar.monmonapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MonmonApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonmonApiApplication.class, args);
	}

}
