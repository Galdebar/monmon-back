package lt.galdebar.monmonapi.webscraper.scheduledtasks;

import lt.galdebar.monmonapi.webscraper.services.helpers.DealsSearchIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RunDealsIndexer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private DealsSearchIndexer searchIndexer;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        searchIndexer.runIndexer();
    }
}
