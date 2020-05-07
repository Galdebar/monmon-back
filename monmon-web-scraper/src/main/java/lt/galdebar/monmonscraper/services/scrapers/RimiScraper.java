package lt.galdebar.monmonscraper.services.scrapers;

import lombok.Getter;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RimiScraper implements IsWebScraper {
    @Getter
    private final ShopNames SHOP = ShopNames.RIMI;

    @Override
    public List<ItemOnOffer> getItemsOnOffer() {
        return null;
    }

    @Override
    public List<ItemOnOffer> getItemsOnOffer(Document document) {
        return null;
    }

    @Override
    public boolean updateOffersDB() {
        return false;
    }

    @Override
    public ShopNames getSHOP() {
        return null;
    }
}
