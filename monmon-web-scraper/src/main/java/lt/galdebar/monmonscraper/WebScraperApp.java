package lt.galdebar.monmonscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"lt.galdebar.monmonscraper","lt.galdebar.monmon.categoriesparser"})
@EnableAsync
public class WebScraperApp {
    public static void main(String[] args) {
        SpringApplication.run(WebScraperApp.class,args);
    }
}
