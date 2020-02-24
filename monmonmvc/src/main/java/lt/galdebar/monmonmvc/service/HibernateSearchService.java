package lt.galdebar.monmonmvc.service;

import com.sun.xml.bind.api.impl.NameConverter;
import lt.galdebar.monmonmvc.persistence.dao.Category;
import lt.galdebar.monmonmvc.persistence.dao.Keyword;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.Query;
import org.hibernate.search.annotations.Analyze;
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
import java.util.List;

@Service
public class HibernateSearchService {
    private final int MAX_RESULTS = 5;

    @PersistenceContext
    private EntityManager entityManager;

    private Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);


    public List<String> searchKeywordAutocomplete(String searchString) {
        List<Keyword> keywordList = searchKeywords(searchString);
        List<String> keywordStrings = new ArrayList<>();
        keywordList.forEach(keyword -> {
            keywordStrings.add(keyword.getKeyword());
        });
        return keywordStrings;
    }

    public Category findCategoryByKeyword(String keyword) {
        List<Keyword> foundKeywords = searchKeywords(keyword);
        if (foundKeywords.size() == 0) {
            return getUncategorized();
        } else return foundKeywords.get(0).getCategory();

    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<Keyword> searchKeywords(String searchString) {
        String analyzedString = analyzeString(searchString);
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(Keyword.class).get();

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(1)
                .withPrefixLength(1)
                .onField("keyword")
                .matching(analyzedString)
                .createQuery();


        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, Keyword.class);

        return jpaQuery.setMaxResults(MAX_RESULTS).getResultList();
    }

    private String analyzeString(String searchString) {
        List<String> result = new ArrayList<>();
        try{
            TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(searchString));
            tokenStream.reset();
            while (tokenStream.incrementToken()){
                result.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
            }
            tokenStream.close();

        } catch (IOException e){
            throw new RuntimeException(e);
        }
        return String.join(" ", result);
    }

    @Transactional
    public Category getUncategorized() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(Category.class).get();

        Query query = queryBuilder
                .keyword()
//                .fuzzy()
//                .withEditDistanceUpTo(1)
//                .withPrefixLength(1)
                .onField("category_name")
                .matching("Uncategorized")
                .createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, Category.class);

        Object result = jpaQuery.setMaxResults(1).getResultList().get(0);
        if (result instanceof Category) {
            return (Category) result;
        } else return null;

    }

    @Transactional
    public List<String> getAllCategories() {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(Category.class).get();

        Query query = queryBuilder.all().createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, Category.class);

        List<Category> categoryList = jpaQuery.getResultList();
        List<String> categoryTitles = new ArrayList<>();
        categoryList.forEach(category -> {
            categoryTitles.add(category.getCategoryName());
        });
        return categoryTitles;
    }

}
