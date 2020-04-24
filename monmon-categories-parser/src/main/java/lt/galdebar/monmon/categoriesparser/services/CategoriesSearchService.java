package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryEntity;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordEntity;
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

@Service
public class CategoriesSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CategoryDTOToEntityConverter categoryConverter;

    @Autowired
    private KeywordDTOToEntityConverter keywordConverter;
    private static final KeywordComparator COMPARATOR = new KeywordComparator();

    @Transactional
    public CategoryDTO searchCategory(CategoryDTO itemCategory) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(CategoryEntity.class);
        String analyzedString = analyzeString(customAnalyzer, itemCategory.getCategoryName());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(CategoryEntity.class).get();

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


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, CategoryEntity.class);
        List<CategoryEntity> queryResults = jpaQuery.getResultList();

        if (queryResults.size() == 0) {
            return getUncategorized();
        }

        if (!queryResults.get(0).getCategoryName().equals(itemCategory.getCategoryName())) {
            return getUncategorized();
        }

        return categoryConverter.convertEntityToDTO(queryResults.get(0));
    }

    @Transactional
    public List<KeywordDTO> findKeywords(KeywordDTO keywordDTO) {
        List<KeywordEntity> foundKeywords = searchKeywords(keywordDTO);

        List<KeywordDTO> keywordDTOS = keywordConverter.convertEntitiesToDTOs(foundKeywords);
        keywordDTOS.sort(COMPARATOR);
        return keywordDTOS;
    }

    @Transactional
    public List<KeywordDTO> findKeywords(KeywordDTO keywordDTO, int maxResults) {
        List<KeywordEntity> foundKeywords = searchKeywords(keywordDTO, maxResults);

        List<KeywordDTO> keywordDTOS = keywordConverter.convertEntitiesToDTOs(foundKeywords);
        keywordDTOS.sort(COMPARATOR);
        return keywordDTOS;
    }

    public List<CategoryDTO> findCategoriesByKeyword(KeywordDTO keywordDTO) {
        List<KeywordEntity> foundKeywords = searchKeywords(keywordDTO);
        List<CategoryEntity> categoryEntities = new ArrayList<>();
        for (KeywordEntity keyword : foundKeywords) {
            categoryEntities.add(keyword.getCategory());
        }
        return categoryConverter.convertEntitiesToDTOs(categoryEntities);
    }

    public List<CategoryDTO> findCategoriesByKeyword(KeywordDTO keywordDTO, int maxResults) {
        List<KeywordEntity> foundKeywords = searchKeywords(keywordDTO, maxResults);
        List<CategoryEntity> categoryEntities = new ArrayList<>();
        for (KeywordEntity keyword : foundKeywords) {
            categoryEntities.add(keyword.getCategory());
        }
        return categoryConverter.convertEntitiesToDTOs(categoryEntities);
    }

    @Transactional
    private List<KeywordEntity> searchKeywords(KeywordDTO keywordDTO) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(KeywordEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keywordDTO.getKeyword());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(KeywordEntity.class).get();

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


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, KeywordEntity.class);
        List<KeywordEntity> foundKeywords = jpaQuery.getResultList();
//        foundKeywords.sort(COMPARATOR);
//        for(KeywordEntity keyword:foundKeywords){
//            System.out.println(keyword.getCategory().getCategoryName());
//        }
        return foundKeywords;
    }

    @Transactional
    private List<KeywordEntity> searchKeywords(KeywordDTO keywordDTO, int maxResults) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(KeywordEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keywordDTO.getKeyword());

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(KeywordEntity.class).get();

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


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, KeywordEntity.class);
        List<KeywordEntity> foundKeywords = jpaQuery.setMaxResults(maxResults).getResultList();
//        foundKeywords.sort(COMPARATOR);
//        for(KeywordEntity keyword:foundKeywords){
//            System.out.println(keyword.getCategory().getCategoryName());
//        }
        return foundKeywords;
    }

    @Transactional
    public CategoryDTO getUncategorized() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(CategoryEntity.class).get();

        Query query = queryBuilder
                .keyword()
                .onField("category_name")
                .matching("Uncategorized")
                .createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, CategoryEntity.class);

        Object result = jpaQuery.setMaxResults(1).getResultList().get(0);
        if (result instanceof CategoryEntity) {
            return categoryConverter.convertEntityToDTO((CategoryEntity) result);
        } else return null;

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
