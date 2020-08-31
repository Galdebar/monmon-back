package lt.galdebar.monmonapi.webscraper.services.helpers;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

@Component
@Log4j2
public class DealsSearchIndexer {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DealsSearchIndexer(final EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Transactional
    public void runIndexer(){
        log.info("Running Shopping Deals Indexer");
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Transactional
    public void purge(){
        log.info("Purging all indexed data");
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        fullTextEntityManager.purgeAll(ShoppingItemDealEntity.class);
    }
}
