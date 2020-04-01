package lt.galdebar.monmonmvc.eventmanagers;

import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ExecuteCategoriesParserListener {

    @Autowired
    private ExcelParser excelParser;

    @EventListener
    public void handleContextStart(ContextStartedEvent event){
        System.out.println("Context started event here");
    }
}
