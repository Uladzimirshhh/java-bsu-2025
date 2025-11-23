package production.line;

import production.model.Product;
import production.validation.Validatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ProductionLine<T extends Product> implements Validatable<T> {

    private final String lineId;
    protected final List<T> products = new ArrayList<>();
    private final double efficiency; // 0.0 - 1.0

    protected ProductionLine(String lineId, double efficiency) {
        if (efficiency < 0.0 || efficiency > 1.0)
            throw new IllegalArgumentException("Efficiency must be between 0.0 and 1.0");
        this.lineId = lineId;
        this.efficiency = efficiency;
    }

    public String getLineId() { return lineId; }
    public List<T> getProducts() { return Collections.unmodifiableList(products); }
    public double getEfficiency() { return efficiency; }

    public abstract boolean canProduce(Product product);

    public void addProduct(T product) {
        validateProduct(product);
        products.add(product);
    }
}
