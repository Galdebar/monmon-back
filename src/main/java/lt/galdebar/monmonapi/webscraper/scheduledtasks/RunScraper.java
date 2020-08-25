package lt.galdebar.monmonapi.webscraper.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.services.WebScraperAPI;
import lt.galdebar.monmonapi.webscraper.services.scrapers.IsWebScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class RunScraper implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private List<IsWebScraper> webScrapers;


    @Override
    @Async
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
//        log.info("Running Web Scrapers.");
//        for(IsWebScraper scraper: webScrapers){
//            log.info("Running scraper for shop: " + scraper.getSHOP());
//            scraper.updateOffersDB();
//        }
    }
}
