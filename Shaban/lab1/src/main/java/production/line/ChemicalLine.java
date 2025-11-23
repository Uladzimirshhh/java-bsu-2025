package production.line;

import production.model.ChemicalProduct;
import production.model.Product;

public class ChemicalLine extends ProductionLine<ChemicalProduct> {

    public ChemicalLine(String lineId, double efficiency) {
        super(lineId, efficiency);
    }

    @Override
    public boolean canProduce(Product product) {
        return product instanceof ChemicalProduct;
    }

    @Override
    public void validateProduct(ChemicalProduct product) {
        if (!canProduce(product)) {
            throw new IllegalArgumentException("This line can produce only chemical products");
        }
    }
}
