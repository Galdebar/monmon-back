package lt.galdebar.monmonscraper.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class ScrapedShoppingItem {
    private final String name;
    private final String brand;
    private final float price;
}
