package lt.galdebar.monmonapi.categoriesparser.services;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.*;
import lt.galdebar.monmonapi.webscraper.services.helpers.StringMatcherHelper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriesSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CategoryDTOToEntityConverter categoryConverter;

    @Autowired
    private KeywordDTOToEntityConverter keywordConverter;

    @Autowired
    private StringMatcherHelper stringMatcher;

    private static final KeywordComparator COMPARATOR = new KeywordComparator();

    @Transactional
    public ShoppingCategoryDTO searchCategory(ShoppingCategoryDTO itemCategory) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(ShoppingCategoryEntity.class);
        String analyzedString = analyzeString(customAnalyzer, itemCategory.getCategoryName());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingCategoryEntity.class).get();

        if (analyzedString.trim().isEmpty()) {
            return getUncategorized();
        }

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(2)
                .withPrefixLength(2)
                .onField("category_name")
                .matching(analyzedString)
                .createQuery();


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingCategoryEntity.class);
        List<ShoppingCategoryEntity> queryResults = jpaQuery.getResultList();

        if (queryResults.size() == 0) {
            return getUncategorized();
        }

        if (!queryResults.get(0).getCategoryName().equals(itemCategory.getCategoryName())) {
            return getUncategorized();
        }

        return categoryConverter.convertEntityToDTO(queryResults.get(0));
    }

    @Transactional
    public List<ShoppingKeywordDTO> findKeywords(ShoppingKeywordDTO keywordDTO) {
        List<ShoppingKeywordEntity> foundKeywords = searchKeywords(keywordDTO);

        List<ShoppingKeywordDTO> keywordDTOS = keywordConverter.convertEntitiesToDTOs(foundKeywords);
        keywordDTOS.sort(COMPARATOR);
        return keywordDTOS;
    }

    @Transactional
    public List<ShoppingKeywordDTO> findKeywords(ShoppingKeywordDTO keywordDTO, int maxResults) {
        List<CustomKeywordEntity> foundCustomKeywords = searchCustomKeywords(keywordDTO,maxResults);

        if(foundCustomKeywords.size() >0){
            return foundCustomKeywords.stream()
                    .map(CustomKeywordEntity::getStandardKeywordDTO)
                    .collect(Collectors.toList());
        }


        List<ShoppingKeywordEntity> foundKeywords = searchKeywords(keywordDTO, maxResults);

        List<ShoppingKeywordDTO> keywordDTOS = keywordConverter.convertEntitiesToDTOs(foundKeywords);
        keywordDTOS.sort(COMPARATOR);
        return keywordDTOS;
    }

    public List<ShoppingCategoryDTO> findCategoriesByKeyword(ShoppingKeywordDTO keywordDTO) {
        List<ShoppingKeywordEntity> foundKeywords = searchKeywords(keywordDTO);
        List<ShoppingCategoryEntity> categoryEntities = new ArrayList<>();
        for (ShoppingKeywordEntity keyword : foundKeywords) {
            categoryEntities.add(keyword.getShoppingItemCategory());
        }
        return categoryConverter.convertEntitiesToDTOs(categoryEntities);
    }

    public List<ShoppingCategoryDTO> findCategoriesByCustomKeyword(ShoppingKeywordDTO keywordDTO) {
        List<CustomKeywordEntity> foundKeywords = searchCustomKeywords(keywordDTO);
        List<ShoppingCategoryEntity> categoryEntities = new ArrayList<>();
        for (CustomKeywordEntity keyword : foundKeywords) {
            categoryEntities.add(keyword.getShoppingItemCategory());
        }
        return categoryConverter.convertEntitiesToDTOs(categoryEntities);
    }


    public List<ShoppingCategoryDTO> findCategoriesByKeyword(ShoppingKeywordDTO keywordDTO, int maxResults) {
        List<ShoppingKeywordEntity> foundKeywords = searchKeywords(keywordDTO, maxResults);
        List<ShoppingCategoryEntity> categoryEntities = new ArrayList<>();
        for (ShoppingKeywordEntity keyword : foundKeywords) {
            categoryEntities.add(keyword.getShoppingItemCategory());
        }
        return categoryConverter.convertEntitiesToDTOs(categoryEntities);
    }

    @Transactional
    private List<ShoppingKeywordEntity> searchKeywords(ShoppingKeywordDTO keywordDTO) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(ShoppingKeywordEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keywordDTO.getKeyword());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingKeywordEntity.class).get();

        if (analyzedString.trim().isEmpty()) {
            return new ArrayList<>();
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

        List<ShoppingKeywordEntity> foundKeywords =  (List<ShoppingKeywordEntity>) jpaQuery.getResultList();
//        String bestMatch = stringMatcher.findBestMatch(
//                keywordDTO.getKeyword(),
//                foundKeywords.stream()
//                .map(ShoppingKeywordEntity::getKeyword)
//                .collect(Collectors.toList())
//        );

        return foundKeywords;
    }

    @Transactional
    private List<ShoppingKeywordEntity> searchKeywords(ShoppingKeywordDTO keywordDTO, int maxResults) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(ShoppingKeywordEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keywordDTO.getKeyword());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingKeywordEntity.class).get();

        if (analyzedString.trim().isEmpty()) {
            return new ArrayList<>();
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
//        List<ShoppingKeywordEntity> foundKeywords = (List<ShoppingKeywordEntity>) jpaQuery.setMaxResults(maxResults).getResultList();
//                foundKeywords.sort(COMPARATOR);
//        for(KeywordEntity keyword:foundKeywords){
//            System.out.println(keyword.getCategory().getCategoryName());
//        }
        return (List<ShoppingKeywordEntity>) jpaQuery.setMaxResults(maxResults).getResultList();
    }

    @Transactional
    private List<CustomKeywordEntity> searchCustomKeywords(ShoppingKeywordDTO keywordDTO) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(CustomKeywordEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keywordDTO.getKeyword());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(CustomKeywordEntity.class).get();

        if (analyzedString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(2)
                .withPrefixLength(2)
                .onField("customKeyword")
                .matching(analyzedString)
                .createQuery();


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, CustomKeywordEntity.class);

        List<CustomKeywordEntity> foundKeywords =  (List<CustomKeywordEntity>) jpaQuery.getResultList();
        return foundKeywords;
    }

    @Transactional
    private List<CustomKeywordEntity> searchCustomKeywords(ShoppingKeywordDTO keywordDTO,int maxResults) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(CustomKeywordEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keywordDTO.getKeyword());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(CustomKeywordEntity.class).get();

        if (analyzedString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(2)
                .withPrefixLength(2)
                .onField("customKeyword")
                .matching(analyzedString)
                .createQuery();


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, CustomKeywordEntity.class);

        List<CustomKeywordEntity> foundKeywords =  (List<CustomKeywordEntity>) jpaQuery.setMaxResults(maxResults).getResultList();
        return foundKeywords;
    }

    @Transactional
    public ShoppingCategoryDTO getUncategorized() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingCategoryEntity.class).get();

        Query query = queryBuilder
                .keyword()
                .onField("category_name")
                .matching("Uncategorized")
                .createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, ShoppingCategoryEntity.class);

        ShoppingCategoryEntity result = (ShoppingCategoryEntity) jpaQuery.setMaxResults(1).getResultList().get(0);
        return categoryConverter.convertEntityToDTO(result);

    }

    private String analyzeString(Analyzer customAnalyzer, String searchString) {
        List<String> result = new ArrayList<>();
        try {
            TokenStream tokenStream = customAnalyzer.tokenStream(null, new StringReader(searchString));
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String string = tokenStream.getAttribute(CharTermAttribute.class).toString();
                if (!string.trim().isEmpty()) {
                    result.add(string);
                }
            }
            tokenStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return String.join(" ", result);
    }
}
