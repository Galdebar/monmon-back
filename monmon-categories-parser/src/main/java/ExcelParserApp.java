import lt.galdebar.monmon.categoriesparser.CategoriesParserMain;

public class ExcelParserApp {

    public static void main(String[] args) {
        CategoriesParserMain categoriesParserMain = new CategoriesParserMain();
        categoriesParserMain.pushCategoriesToDB();
    }
}
