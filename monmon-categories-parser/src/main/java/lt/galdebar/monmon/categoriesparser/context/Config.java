package lt.galdebar.monmon.categoriesparser.context;

import lt.galdebar.monmon.categoriesparser.services.CategoriesParserMain;
import lt.galdebar.monmon.categoriesparser.services.CategoryDTOtoDAOConverter;
import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ExcelParser excelParser(){
        return new ExcelParser("src/main/resources/taxonomy-with-ids.en-US.xls");
    }
}
