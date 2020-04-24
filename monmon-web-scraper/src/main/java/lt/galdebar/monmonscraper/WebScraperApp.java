package lt.galdebar.monmonscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"lt.galdebar.monmonscraper","lt.galdebar.monmon.categoriesparser"})
public class WebScraperApp {
    public static void main(String[] args) {
        SpringApplication.run(WebScraperApp.class,args);
    }
}
