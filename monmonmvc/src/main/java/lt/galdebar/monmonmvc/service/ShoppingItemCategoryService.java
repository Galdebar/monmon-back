package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingCategoryDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingKeywordDAO;
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
    private final int MAX_RESULTS = 5;

    @PersistenceContext
    private EntityManager entityManager;

    private Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);


    public List<ShoppingKeywordDTO> searchKeywordAutocomplete(ShoppingKeywordDTO keywordDTO) {
        List<ShoppingKeywordDAO> keywordList = searchKeywords(keywordDTO);
        return keywordDAOSToDTOS(keywordList);
    }

    public ShoppingCategoryDTO findCategoryByKeyword(ShoppingKeywordDTO keywordDTO) {
        List<ShoppingKeywordDAO> foundKeywords = searchKeywords(keywordDTO);
        if (foundKeywords.size() == 0
        || !foundKeywords.get(0).getKeyword().equalsIgnoreCase(keywordDTO.getKeyword())) {
            System.out.println(foundKeywords.get(0).getShoppingItemCategory().getCategoryName());
            return categoryDAOToDTO(getUncategorized());
        } else return categoryDAOToDTO(foundKeywords.get(0).getShoppingItemCategory());

    }

    @SuppressWarnings("unchecked")
    @Transactional
    private List<ShoppingKeywordDAO> searchKeywords(ShoppingKeywordDTO keywordDTO) {
        String analyzedString = analyzeString(keywordDTO.getKeyword());
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingKeywordDAO.class).get();

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(1)
                .withPrefixLength(1)
                .onField("keyword")
                .matching(analyzedString)
                .createQuery();


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingKeywordDAO.class);

        return jpaQuery.setMaxResults(MAX_RESULTS).getResultList();
    }

    private String analyzeString(String searchString) {
        List<String> result = new ArrayList<>();
        try {
            TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(searchString));
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
            }
            tokenStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return String.join(" ", result);
    }

    @Transactional
    public ShoppingCategoryDAO getUncategorized() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingCategoryDAO.class).get();

        Query query = queryBuilder
                .keyword()
                .onField("category_name")
                .matching("Uncategorized")
                .createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingCategoryDAO.class);

        Object result = jpaQuery.setMaxResults(1).getResultList().get(0);
        if (result instanceof ShoppingCategoryDAO) {
            return (ShoppingCategoryDAO) result;
        } else return null;

    }

    @Transactional
    public List<ShoppingCategoryDTO> getAllCategories() {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingCategoryDAO.class).get();

        Query query = queryBuilder.all().createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingCategoryDAO.class);

        List<ShoppingCategoryDAO> shoppingItemCategoryList = jpaQuery.getResultList();

        return categoryDAOSToDTOS(shoppingItemCategoryList);
    }

    private ShoppingCategoryDTO categoryDAOToDTO(ShoppingCategoryDAO shoppingCategoryDAO) {
        Set<String> keywords = new HashSet<>();
        shoppingCategoryDAO.getKeywords().forEach(shoppingKeywordDAO -> {
            keywords.add(shoppingKeywordDAO.getKeyword());
        });

        return new ShoppingCategoryDTO(shoppingCategoryDAO.getCategoryName(), keywords);
    }

    private List<ShoppingCategoryDTO> categoryDAOSToDTOS(List<ShoppingCategoryDAO> shoppingCategoryDAOList){
        List<ShoppingCategoryDTO> shoppingCategoryDTOList = new ArrayList<>();
        shoppingCategoryDAOList.forEach(categoryDAO -> {
            shoppingCategoryDTOList.add(categoryDAOToDTO(categoryDAO));
        });
        return shoppingCategoryDTOList;
    }

    private ShoppingCategoryDAO categoryDTOToDAO(ShoppingCategoryDTO shoppingCategoryDTO) {
        ShoppingCategoryDAO shoppingCategoryDAO = new ShoppingCategoryDAO();
        shoppingCategoryDAO.setCategoryName(shoppingCategoryDTO.getCategoryName());
        for(String keyword: shoppingCategoryDTO.getKeywords()){
            ShoppingKeywordDAO keywordDAO = new ShoppingKeywordDAO();
            keywordDAO.setKeyword(keyword);
            shoppingCategoryDAO.getKeywords().add(keywordDAO);
        }

        return shoppingCategoryDAO;
    }

    private List<ShoppingCategoryDAO> categoryDTOSToDAOS(List<ShoppingCategoryDTO> shoppingCategoryDTOList){
        List<ShoppingCategoryDAO> shoppingCategoryDAOList = new ArrayList<>();
        shoppingCategoryDTOList.forEach(categoryDTO -> {
            shoppingCategoryDAOList.add(categoryDTOToDAO(categoryDTO));
        });

        return shoppingCategoryDAOList;
    }

    private List<ShoppingKeywordDTO> keywordDAOSToDTOS(List<ShoppingKeywordDAO> keywordList) {
        List<ShoppingKeywordDTO> shoppingKeywordDTOList = new ArrayList<>();
        keywordList.forEach(keywordDAO -> {
            shoppingKeywordDTOList.add(keywordDAOToDTO(keywordDAO));
        });

        return shoppingKeywordDTOList;
    }

    private ShoppingKeywordDTO keywordDAOToDTO(ShoppingKeywordDAO shoppingKeywordDAO) {
        return new ShoppingKeywordDTO(
                shoppingKeywordDAO.getShoppingItemCategory().getCategoryName(),
                shoppingKeywordDAO.getKeyword()
        );
    }


}
