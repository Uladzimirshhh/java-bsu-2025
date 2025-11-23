package production.line;

import production.model.ElectronicProduct;
import production.model.Product;

public class ElectronicsLine extends ProductionLine<ElectronicProduct> {

    public ElectronicsLine(String lineId, double efficiency) {
        super(lineId, efficiency);
    }

    @Override
    public boolean canProduce(Product product) {
        return product instanceof ElectronicProduct;
    }

    @Override
    public void validateProduct(ElectronicProduct product) {
        if (!canProduce(product)) {
            throw new IllegalArgumentException("This line can produce only electronic products");
        }
    }
}
