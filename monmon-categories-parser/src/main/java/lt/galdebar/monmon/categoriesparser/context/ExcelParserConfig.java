package lt.galdebar.monmon.categoriesparser.context;

import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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
     * @throws IOException the io exception
     */
    @Bean
    public ExcelParser excelParser() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:taxonomy-with-ids.en-US.xls");
        return new ExcelParser(resource.getFile());
    }
}
