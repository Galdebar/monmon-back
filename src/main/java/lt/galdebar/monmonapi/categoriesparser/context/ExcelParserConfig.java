package lt.galdebar.monmonapi.categoriesparser.context;

import lt.galdebar.monmonapi.categoriesparser.services.ExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * Excel Parser config.
 */
@Configuration
public class ExcelParserConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Raises the Excel parser bean with the correct excel file by creating a Resource object.
     *
     * @return the excel parser
     */
    @Bean
    public ExcelParser excelParser() {
        Resource resource = resourceLoader.getResource("classpath:taxonomy-with-ids.en-US.xls");
        return new ExcelParser(resource);
    }
}
