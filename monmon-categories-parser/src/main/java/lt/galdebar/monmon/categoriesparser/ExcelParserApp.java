package lt.galdebar.monmon.categoriesparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ExcelParserApp {

    public static void main(String[] args) {
        SpringApplication.run(ExcelParserApp.class,args);
        System.out.println("Excel parser runs");
    }
}
