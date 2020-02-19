import lt.galdebar.monmon.categoriesparser.CategoriesParserMain;

public class App {

    public static void main(String[] args) {
        CategoriesParserMain categoriesParserMain = new CategoriesParserMain();
        categoriesParserMain.pushCategoriesToDB();
    }
}
