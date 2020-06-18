package lt.galdebar.monmonapi;

import lt.galdebar.shoppingitems.EnableShoppingItemsModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableShoppingItemsModule
public class MonmonApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonmonApiApplication.class, args);
	}

}
