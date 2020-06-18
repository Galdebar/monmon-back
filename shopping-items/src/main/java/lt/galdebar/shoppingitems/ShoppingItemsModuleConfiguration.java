package lt.galdebar.shoppingitems;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Log4j2
@ComponentScan(basePackages = "lt.galdebar.shoppingitems")
public class ShoppingItemsModuleConfiguration {

    @PostConstruct
    public void postConstruct(){
        log.info("SHOPPING ITEMS MODULe LOADED");
    }
}
