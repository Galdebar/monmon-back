package lt.galdebar.monmonmvc.scheduledtasks;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmon.categoriesparser.services.CategoriesParserMain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Handles tasks scheduled for context refresh.<br>
 *     Runs the categories parser in case the application starts in an environment where the categories DB is empty.
 */
@Component
//@ComponentScan(basePackages = "lt.galdebar.monmon.categoriesparser")
@Log4j2
public class ContextRefreshTasks implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private CategoriesParserMain categoriesParser;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("Running tasks on Context Refresh");
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
