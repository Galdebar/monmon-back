package lt.galdebar.monmonmvc;

import lt.galdebar.monmon.categoriesparser.CategoriesParserMain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonmonmvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonmonmvcApplication.class, args);

		System.out.println("app runs!");

		CategoriesParserMain categoriesParser = new CategoriesParserMain();
		categoriesParser.pushCategoriesToDB();
	}

}
