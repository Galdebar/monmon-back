package lt.galdebar.monmonapi.webscraper.services.scrapers.pojos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public
class ItemOnOffer {
    private final String name;
    private final String brand;
    private final float price;
    private final String shopName;
}
