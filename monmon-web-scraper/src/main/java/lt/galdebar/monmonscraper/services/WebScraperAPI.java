package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.services.scrapers.IsWebScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebScraperAPI {

    @Autowired
    private List<IsWebScraper> webScrapers;

    public List<IsWebScraper> getAvailableScrapers() {
        return webScrapers;
    }

    public void runScrapers() {
        for (IsWebScraper scraper : webScrapers) {
            scraper.updateOffersDB();
        }
    }
}
