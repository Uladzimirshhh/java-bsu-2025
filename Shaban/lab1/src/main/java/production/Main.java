package production;

import production.analyzer.ProdAnalyzer;
import production.line.*;
import production.model.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var eLine = new ElectronicsLine("EL-1", 0.95);
        eLine.addProduct(new ElectronicProduct("E1", "Smartphone", 120));
        eLine.addProяduct(new ElectronicProduct("E2", "Laptop", 240));

        var mLine = new MechanicalLine("ML-1", 0.82);
        mLine.addProduct(new MechanicalProduct("M1", "Engine", 360));

        var cLine = new ChemicalLine("CL-1", 0.76);
        cLine.addProduct(new ChemicalProduct("C1", "Paint", 180));
        cLine.addProduct(new ChemicalProduct("C2", "Solvent", 90));

        var analyzer = new ProdAnalyzer(List.of(eLine, mLine, cLine));

        System.out.println("⚙️ Линии с высокой эффективностью (>0.8): " + analyzer.getHighEfficiencyLines(0.8));
        System.out.println("📦 Кол-во изделий по категориям: " + analyzer.countProductsByCategory());
        System.out.println("📈 Самая загруженная линия: " + analyzer.findMostLoadedLine().map(ProductionLine::getLineId).orElse("Нет данных"));
        System.out.println("🧾 Все изделия: " + analyzer.getAllProductsFromLines());
        System.out.println("⏱️ Общее время производства: " + analyzer.calculateTotalProductionTime() + " мин");
    }
}
