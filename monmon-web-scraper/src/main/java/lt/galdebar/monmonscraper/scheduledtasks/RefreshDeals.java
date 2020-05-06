package lt.galdebar.monmonscraper.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.services.WebScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;

@Log4j2
public class RefreshDeals {

    @Autowired
    private WebScraperAPI webScraperAPI;
    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    @Scheduled(cron = "0 0 0 * * *" )
    public void refreshDeals(){
        log.info("Refreshing Deals");
        dealsRepo.deleteAll();
        webScraperAPI.runScrapers();
    }
}
