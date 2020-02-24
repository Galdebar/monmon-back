package lt.galdebar.monmon.categoriesparser;

import lt.galdebar.monmon.categoriesparser.domain.CategoryDTOtoDAOService;
import lt.galdebar.monmon.categoriesparser.excell.ExcelParser;

public class GlobalDependencies {
    public static final ExcelParser EXCEL_PARSER = new ExcelParser();
    public static final CategoryDTOtoDAOService CONVERTER_SERVICE = new CategoryDTOtoDAOService();
    public static final SessionManager SESSION_MANAGER = new SessionManager();

}
