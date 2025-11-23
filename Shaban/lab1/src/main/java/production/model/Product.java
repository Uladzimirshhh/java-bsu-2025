package production.model;

public abstract class Product {
    private final String id;
    private final String name;
    private final int productionTimeMinutes;

    protected Product(String id, String name, int productionTimeMinutes) {
        this.id = id;
        this.name = name;
        this.productionTimeMinutes = productionTimeMinutes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getProductionTimeMinutes() { return productionTimeMinutes; }

    public abstract String getCategory();

    @Override
    public String toString() {
        return "%s (%s, %d min)".formatted(name, getCategory(), productionTimeMinutes);
    }
}
