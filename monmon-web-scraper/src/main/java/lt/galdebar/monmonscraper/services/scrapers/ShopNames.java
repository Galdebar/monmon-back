package lt.galdebar.monmonscraper.services.scrapers;

public enum ShopNames {
    MAXIMA("Maxima"),
    RIMI("Rimi"),
    IKI("Iki"),
    NORFA("Norfa");

    private String shopName;

    public String getShopName() {
        return shopName;
    }

    ShopNames(String shopName) {
        this.shopName = shopName;
    }
}
