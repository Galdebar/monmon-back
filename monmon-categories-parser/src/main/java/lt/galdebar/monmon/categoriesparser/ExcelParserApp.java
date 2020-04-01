package lt.galdebar.monmon.categoriesparser;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
//@Log4j2
public class ExcelParserApp {

    public static void main(String[] args) {
        SpringApplication.run(ExcelParserApp.class,args);
//        log.info("Excel parser module started.");
        System.out.println("Parser runs.");
    }
}
