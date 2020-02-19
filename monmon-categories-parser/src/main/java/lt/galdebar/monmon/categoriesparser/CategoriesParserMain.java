package lt.galdebar.monmon.categoriesparser;

import lt.galdebar.monmon.categoriesparser.domain.CategoryDAO;
import lt.galdebar.monmon.categoriesparser.excell.CategoryDTO;

import java.util.List;

public class CategoriesParserMain {

    public void pushCategoriesToDB(){
        List<CategoryDAO> categoryDAOList = convertToDAO(getCategories());
//        for(CategoryDAO categoryDAO: categoryDAOList){
//            System.out.println(categoryDAO);
//        }
//        GlobalDependencies.SESSION_MANAGER.pushList(categoryDAOList);
    }

    private List<CategoryDTO> getCategories(){
        if(GlobalDependencies.EXCELL_PARSER.isParserValid()){
            return GlobalDependencies.EXCELL_PARSER.getCategories();
        }

        return null;
    }

    private List<CategoryDAO> convertToDAO(List<CategoryDTO> categoryDTOList){
        return GlobalDependencies.CONVERTER_SERVICE.convertList(categoryDTOList);
    }
}
