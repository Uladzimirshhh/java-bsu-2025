package production.analyzer;

import production.model.Product;
import production.line.ProductionLine;

import java.util.*;
import java.util.stream.Collectors;

public class ProdAnalyzer {

    private final List<ProductionLine<? extends Product>> lines;

    public ProdAnalyzer(List<ProductionLine<? extends Product>> lines) {
        this.lines = lines;
    }

    public List<ProductionLine<? extends Product>> getAllLines() {
        return lines;
    }

    public List<String> getHighEfficiencyLines(double threshold) {
        return lines.stream()
                .filter(line -> line.getEfficiency() > threshold)
                .map(ProductionLine::getLineId)
                .toList();
    }

    public Map<String, Long> countProductsByCategory() {
        return lines.stream()
                .flatMap(line -> line.getProducts().stream())
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
    }

    public Optional<ProductionLine<? extends Product>> findMostLoadedLine() {
        return lines.stream()
                .max(Comparator.comparingInt(line -> line.getProducts().size()));
    }

    public List<Product> getAllProductsFromLines() {
        return lines.stream()
                .flatMap(line -> line.getProducts().stream())
                .map(p -> (Product) p)
                .toList();
    }

    public double calculateTotalProductionTime() {
        return lines.stream()
                .flatMap(line -> line.getProducts().stream())
                .mapToDouble(Product::getProductionTimeMinutes)
                .sum();
    }
}
