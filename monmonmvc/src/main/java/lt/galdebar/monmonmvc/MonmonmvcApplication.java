package lt.galdebar.monmonmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.function.Function;

@SpringBootApplication
public class MonmonmvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonmonmvcApplication.class, args);

		System.out.println("app runs!");

//		CategoriesParserMain categoriesParser = new CategoriesParserMain();
//		categoriesParser.pushCategoriesToDB();


	}

}
