package lt.galdebar.monmon.categoriesparser.context;

import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;

@Configuration
public class Config {

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public ExcelParser excelParser() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:taxonomy-with-ids.en-US.xls");
        return new ExcelParser(resource.getFile());
    }
}
