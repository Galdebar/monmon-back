package lt.galdebar.monmonapi.categoriesparser.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.categoriesparser.services.CategoriesSearchIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Log4j2
public class RunCategoriesIndexer implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private CategoriesSearchIndexer searchIndexer;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        searchIndexer.runIndexer();
    }
}
