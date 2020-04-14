package lt.galdebar.monmonscraper.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public
class ScrapedShoppingItem {
    private final String name;
    private final String brand;
    private final float price;
}
