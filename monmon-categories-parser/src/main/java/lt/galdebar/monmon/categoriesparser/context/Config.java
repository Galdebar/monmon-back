package lt.galdebar.monmon.categoriesparser.context;

import lt.galdebar.monmon.categoriesparser.services.CategoriesParserMain;
import lt.galdebar.monmon.categoriesparser.services.CategoryDTOtoDAOConverter;
import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class Config {

    @Bean
    public ExcelParser excelParser(@Value("${excel.file.location}") Resource fileLocation) throws IOException {
        return new ExcelParser(fileLocation.getFile().getPath());
    }
}
