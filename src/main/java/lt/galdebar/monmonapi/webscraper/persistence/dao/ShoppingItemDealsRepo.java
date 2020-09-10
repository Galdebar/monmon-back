package lt.galdebar.monmonapi.webscraper.persistence.dao;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingItemDealsRepo extends JpaRepository<ShoppingItemDealEntity, Long> {
    List<ShoppingItemDealEntity> findByShopTitle(String shopTitle);
    List<ShoppingItemDealEntity> findByUntranslatedTitle(String untranslatedTitle);
    boolean existsShoppingItemDealEntityByUntranslatedTitle(String untranslatedTitle);
    boolean existsShoppingItemDealEntityByUntranslatedTitleAndShopTitle(String untranslatedTitle, String shopTitle);
    boolean existsShoppingItemDealEntityByTitle(String title);
}
