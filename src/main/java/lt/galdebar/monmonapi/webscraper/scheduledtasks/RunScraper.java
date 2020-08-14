package lt.galdebar.monmonapi.webscraper.scheduledtasks;

import lt.galdebar.monmonapi.webscraper.services.WebScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class RunScraper implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private WebScraperAPI webScraperAPI;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
//        webScraperAPI.runScrapers();
    }
}
