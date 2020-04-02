package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingCategoryEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingKeywordEntity;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShoppingItemCategoryService {
    private final int MAX_RESULTS = 10;

    @PersistenceContext
    private EntityManager entityManager;

    private Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);


    public List<ShoppingKeywordDTO> searchKeywordAutocomplete(ShoppingKeywordDTO keywordDTO) {
        List<ShoppingKeywordEntity> keywordList = searchKeywords(keywordDTO);
        return keywordDAOSToDTOS(keywordList);
    }

    public ShoppingCategoryDTO findCategoryByKeyword(ShoppingKeywordDTO keywordDTO) {
        List<ShoppingKeywordEntity> foundKeywords = searchKeywords(keywordDTO);
        if (foundKeywords.size() == 0
        || !foundKeywords.get(0).getKeyword().equalsIgnoreCase(keywordDTO.getKeyword())) {
            return categoryDAOToDTO(getUncategorized());
        } else return categoryDAOToDTO(foundKeywords.get(0).getShoppingItemCategory());

    }

    @SuppressWarnings("unchecked")
    @Transactional
    private List<ShoppingKeywordEntity> searchKeywords(ShoppingKeywordDTO keywordDTO) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(ShoppingKeywordEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keywordDTO.getKeyword());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingKeywordEntity.class).get();

        if(analyzedString.trim().isEmpty()){
            return new ArrayList<ShoppingKeywordEntity>();
        }

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(2)
                .withPrefixLength(2)
                .onField("keyword")
                .matching(analyzedString)
                .createQuery();


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingKeywordEntity.class);

        return jpaQuery.setMaxResults(MAX_RESULTS).getResultList();
    }

    private String analyzeString(Analyzer customAnalyzer, String searchString) {
        List<String> result = new ArrayList<>();
        try {
            TokenStream tokenStream = customAnalyzer.tokenStream(null, new StringReader(searchString));
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String string = tokenStream.getAttribute(CharTermAttribute.class).toString();
                if(!string.trim().isEmpty()){
                    result.add(string);
                }
            }
            tokenStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return String.join(" ", result);
    }

    @Transactional
    private ShoppingCategoryEntity getUncategorized() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingCategoryEntity.class).get();

        Query query = queryBuilder
                .keyword()
                .onField("category_name")
                .matching("Uncategorized")
                .createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingCategoryEntity.class);

        Object result = jpaQuery.setMaxResults(1).getResultList().get(0);
        if (result instanceof ShoppingCategoryEntity) {
            return (ShoppingCategoryEntity) result;
        } else return null;

    }

    @Transactional
    public List<ShoppingCategoryDTO> getAllCategories() {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingCategoryEntity.class).get();

        Query query = queryBuilder.all().createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingCategoryEntity.class);

        List<ShoppingCategoryEntity> shoppingItemCategoryList = jpaQuery.getResultList();

        return categoryDAOSToDTOS(shoppingItemCategoryList);
    }

    private ShoppingCategoryDTO categoryDAOToDTO(ShoppingCategoryEntity shoppingCategoryEntity) {
        Set<String> keywords = new HashSet<>();
        shoppingCategoryEntity.getKeywords().forEach(shoppingKeywordEntity -> keywords.add(shoppingKeywordEntity.getKeyword()));

        return new ShoppingCategoryDTO(shoppingCategoryEntity.getCategoryName(), keywords);
    }

    private List<ShoppingCategoryDTO> categoryDAOSToDTOS(List<ShoppingCategoryEntity> shoppingCategoryEntityList){
        List<ShoppingCategoryDTO> shoppingCategoryDTOList = new ArrayList<>();
        shoppingCategoryEntityList.forEach(categoryDAO -> shoppingCategoryDTOList.add(categoryDAOToDTO(categoryDAO)));
        return shoppingCategoryDTOList;
    }

    private ShoppingCategoryEntity categoryDTOToDAO(ShoppingCategoryDTO shoppingCategoryDTO) {
        ShoppingCategoryEntity shoppingCategoryEntity = new ShoppingCategoryEntity();
        shoppingCategoryEntity.setCategoryName(shoppingCategoryDTO.getCategoryName());
        for(String keyword: shoppingCategoryDTO.getKeywords()){
            ShoppingKeywordEntity keywordDAO = new ShoppingKeywordEntity();
            keywordDAO.setKeyword(keyword);
            shoppingCategoryEntity.getKeywords().add(keywordDAO);
        }

        return shoppingCategoryEntity;
    }

    private List<ShoppingCategoryEntity> categoryDTOSToDAOS(List<ShoppingCategoryDTO> shoppingCategoryDTOList){
        List<ShoppingCategoryEntity> shoppingCategoryEntityList = new ArrayList<>();
        shoppingCategoryDTOList.forEach(categoryDTO -> shoppingCategoryEntityList.add(categoryDTOToDAO(categoryDTO)));

        return shoppingCategoryEntityList;
    }

    private List<ShoppingKeywordDTO> keywordDAOSToDTOS(List<ShoppingKeywordEntity> keywordList) {
        List<ShoppingKeywordDTO> shoppingKeywordDTOList = new ArrayList<>();
        keywordList.forEach(keywordDAO -> shoppingKeywordDTOList.add(keywordDAOToDTO(keywordDAO)));

        return shoppingKeywordDTOList;
    }

    private ShoppingKeywordDTO keywordDAOToDTO(ShoppingKeywordEntity shoppingKeywordEntity) {
        return new ShoppingKeywordDTO(
                shoppingKeywordEntity.getShoppingItemCategory().getCategoryName(),
                shoppingKeywordEntity.getKeyword()
        );
    }


}
