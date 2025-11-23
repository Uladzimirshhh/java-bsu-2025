package production.line;

import production.model.MechanicalProduct;
import production.model.Product;

public class MechanicalLine extends ProductionLine<MechanicalProduct> {

    public MechanicalLine(String lineId, double efficiency) {
        super(lineId, efficiency);
    }

    @Override
    public boolean canProduce(Product product) {
        return product instanceof MechanicalProduct;
    }

    @Override
    public void validateProduct(MechanicalProduct product) {
        if (!canProduce(product)) {
            throw new IllegalArgumentException("This line can produce only mechanical products");
        }
    }
}
