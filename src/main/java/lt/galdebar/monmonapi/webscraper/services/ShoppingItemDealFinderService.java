package lt.galdebar.monmonapi.webscraper.services;

import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonapi.webscraper.services.helpers.StringMatcherHelper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShoppingItemDealFinderService {

    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    @Autowired
    private StringMatcherHelper stringMatcher;

    @Autowired
    private EntityManager entityManager;

    private final int MAX_QUERY_RESULTS = 5;


    @Transactional
    public List<ShoppingItemDealDTO> getAllDeals() {

        return dealsRepo.findAll().stream()
                .map(ShoppingItemDealEntity::getDTO)
                .collect(Collectors.toList());
    }

    public List<ShoppingItemDealDTO> getDealByShop(ShopNames shop) {
        return dealsRepo.findByShopTitle(shop.getShopName()).stream()
                .map(ShoppingItemDealEntity::getDTO)
                .collect(Collectors.toList());
    }

    public List<ShoppingItemDealDTO> getDealsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return searchDealsByTranslatedTitle(keyword);
    }

    public ShoppingItemDealDTO getBestDeal(String keyword) {
        List<ShoppingItemDealDTO> foundDeals = searchDealsByUntranslatedTitle(keyword);

        if(foundDeals.size() >0){
            ShoppingItemDealDTO bestDeal = findBestDeal(foundDeals);
            bestDeal.setTitle(bestDeal.getUntranslatedTitle());
            return bestDeal;
        }
        foundDeals = searchDealsByTranslatedTitle(keyword);
        return findBestDeal(foundDeals);
    }

    private ShoppingItemDealDTO findBestDeal(List<ShoppingItemDealDTO> deals){
        if (deals.size() == 0) {
            return new ShoppingItemDealDTO();
        }
        if (deals.size() > 1) {
            ShoppingItemDealDTO bestDeal = deals.stream()
                    .sorted((o1, o2) -> Float.compare(o1.getPrice(), o2.getPrice()))
                    .collect(Collectors.toList())
                    .get(0);
            return bestDeal;
        } else return deals.get(0);
    }

    @Transactional
    public List<ShoppingItemDealDTO> searchDealsByTranslatedTitle(String keyword){
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(ShoppingItemDealEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keyword);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingItemDealEntity.class).get();

        if(analyzedString.trim().isEmpty()){
            return new ArrayList<ShoppingItemDealDTO>();
        }

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(2)
                .withPrefixLength(2)
                .onField("title")
                .matching(analyzedString)
                .createQuery();

        FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(
                query,
                ShoppingItemDealEntity.class
        );

        List<ShoppingItemDealEntity> queryResults = jpaQuery.setMaxResults(MAX_QUERY_RESULTS).getResultList();
        return queryResults.stream()
                .map(ShoppingItemDealEntity::getDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ShoppingItemDealDTO> searchDealsByUntranslatedTitle(String keyword){
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        Analyzer customAnalyzer = fullTextEntityManager.getSearchFactory()
                .getAnalyzer(ShoppingItemDealEntity.class);
        String analyzedString = analyzeString(customAnalyzer, keyword);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ShoppingItemDealEntity.class).get();

        if(analyzedString.trim().isEmpty()){
            return new ArrayList<ShoppingItemDealDTO>();
        }

        Query query = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(1)
                .withPrefixLength(1)
                .onField("untranslatedTitle")
                .matching(analyzedString)
                .createQuery();

        FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(
                query,
                ShoppingItemDealEntity.class
        );

        List<ShoppingItemDealEntity> queryResults = jpaQuery.setMaxResults(MAX_QUERY_RESULTS).getResultList();
        return queryResults.stream()
                .map(ShoppingItemDealEntity::getDTO)
                .collect(Collectors.toList());
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
