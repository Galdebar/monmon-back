package lt.galdebar.monmon.categoriesparser.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmon.categoriesparser.services.CategoriesParserAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class RunParser implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private CategoriesParserAPI categoriesParser;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("Running Categories Parser");
        runExcelParser();
    }

    private void runExcelParser() {
        if(categoriesParser.isParserValid()){
            log.info("Running Shopping Item Categories Parser");
            categoriesParser.pushCategoriesToDB();
        } else{
            log.error("Excel Parser Invalid, cannot retrieve categories!");
        }
    }
}
