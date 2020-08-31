package lt.galdebar.monmonapi.categoriesparser.services;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

@Component
@Log4j2
public class CategoriesSearchIndexer {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public CategoriesSearchIndexer(final EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Transactional
    public void runIndexer(){
        log.info("Running Categories and Keywords Indexer");
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
