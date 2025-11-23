package production.validation;

import production.model.Product;

public interface Validatable<T extends Product> {
    void validateProduct(T product);
}
