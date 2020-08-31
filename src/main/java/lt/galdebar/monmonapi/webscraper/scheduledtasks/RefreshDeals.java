package lt.galdebar.monmonapi.webscraper.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.services.WebScraperAPI;
import lt.galdebar.monmonapi.webscraper.services.helpers.DealsSearchIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@Log4j2
public class RefreshDeals {

    @Autowired
    private WebScraperAPI webScraperAPI;
    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    @Autowired
    private DealsSearchIndexer searchIndexer;

    @Scheduled(cron = "0 0 0 * * *" )
    public void refreshDeals(){
        log.info("Refreshing Deals");
        dealsRepo.deleteAll();
        searchIndexer.purge();
        webScraperAPI.runScrapers();
    }
}
