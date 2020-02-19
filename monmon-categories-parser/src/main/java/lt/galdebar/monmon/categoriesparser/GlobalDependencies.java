package lt.galdebar.monmon.categoriesparser;

import lt.galdebar.monmon.categoriesparser.domain.CategoryDTOtoDAOService;
import lt.galdebar.monmon.categoriesparser.excell.ExcellParser;

public class GlobalDependencies {
    public static final ExcellParser EXCELL_PARSER = new ExcellParser();
    public static final CategoryDTOtoDAOService CONVERTER_SERVICE = new CategoryDTOtoDAOService();
    public static final SessionManager SESSION_MANAGER = new SessionManager();

}
