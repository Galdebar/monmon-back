package lt.galdebar.monmonscraper.services;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonscraper.services.scrapers.IsWebScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class WebScraperAPI {

    @Autowired
    private List<IsWebScraper> webScrapers;

    List<IsWebScraper> getAvailableScrapers() {
        return webScrapers;
    }

    @Async
    public void runScrapers() {
        log.info("Running scrapers.");
        for (IsWebScraper scraper : webScrapers) {
            log.info("Running scraper for shop: " + scraper.getSHOP());
            scraper.updateOffersDB();
        }
    }
}
