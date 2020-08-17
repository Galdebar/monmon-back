package lt.galdebar.monmonapi.webscraper.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.services.WebScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class RunScraper implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private WebScraperAPI webScraperAPI;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("Running on application refresh tasks.");
        webScraperAPI.runScrapers();
    }
}
