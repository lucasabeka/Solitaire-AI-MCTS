package ia;

public class AIParams {
    // Nos valeurs par défaut actuelles (L'ADN de base)
    /*
    public double foundationBonus = 1000.0;
    public double revealBonus = 500.0;
    public double tableauMoveBonus = 50.0;
    public double wasteToTableauBonus = 70.0;
    public double kingBonus = 100.0; // Bonus Roi sur vide
    public double stockPenalty = -5.0; // Pour le score moyen
    public double stockPenaltyImmediate = -10.0; // Pour l'évaluation immédiate
    public double recyclePenalty = -100.0;        // <-- AJOUTÉ
    public double foundationToTableauPenalty = -90000000000000000000000.0; // <-- AJOUTÉ

     */

    // --- L'ADN DE L'IA (Paramètres configurables) ---

    // Bonus
    public double foundationBonus = 1000.0;
    public double revealBonus = 500.0;
    public double tableauMoveBonus = 50.0;
    public double wasteToTableauBonus = 70.0;
    public double kingBonus = 100.0; // Bonus Roi sur vide

    // Malus / Pénalités
    public double stockPenalty = -5.0;            // Pour le score moyen (MCTS)
    public double stockPenaltyImmediate = -10.0;  // Pour l'évaluation immédiate
    public double recyclePenalty = -100.0;

    // PÉNALITÉ ATOMIQUE : On interdit pratiquement le retour arrière
    // Notation scientifique : -9 * 10 puissance 22
    public double foundationToTableauPenalty = -9e22;

    // Constructeur par défaut
    public AIParams() {}

    // Constructeur pour créer un mutant
    public AIParams(double foundation, double reveal, double tableau, double waste, double king, double stock, double stockImm, double recycle, double f2t) {
        this.foundationBonus = foundation;
        this.revealBonus = reveal;
        this.tableauMoveBonus = tableau;
        this.wasteToTableauBonus = waste;
        this.kingBonus = king;
        this.stockPenalty = stock;
        this.stockPenaltyImmediate = stockImm;
        this.recyclePenalty = recycle;
        this.foundationToTableauPenalty = f2t;
    }

    // Créer une version mutante (légèrement modifiée)
    public AIParams mutate() {
        AIParams mutant = new AIParams();
        mutant.foundationBonus = mutateValue(this.foundationBonus);
        mutant.revealBonus = mutateValue(this.revealBonus);
        mutant.tableauMoveBonus = mutateValue(this.tableauMoveBonus);
        mutant.wasteToTableauBonus = mutateValue(this.wasteToTableauBonus);
        mutant.kingBonus = mutateValue(this.kingBonus);

        mutant.stockPenalty = mutateValue(this.stockPenalty);
        mutant.stockPenaltyImmediate = mutateValue(this.stockPenaltyImmediate);
        mutant.recyclePenalty = mutateValue(this.recyclePenalty);         // <-- AJOUTÉ
        mutant.foundationToTableauPenalty = mutateValue(this.foundationToTableauPenalty); // <-- AJOUTÉ

        return mutant;
    }

    private double mutateValue(double val) {
        // Modifie la valeur de +/- 10% au hasard
        double change = (Math.random() - 0.5) * 0.2 * val;
        return val + change;
    }

    @Override
    public String toString() {
        return String.format(
                "Foundation: %.1f, Reveal: %.1f, Tableau: %.1f, WasteToTab: %.1f, King: %.1f, Stock: %.1f, StockImm: %.1f, Recycle: %.1f, FndToTab: %.1f",
                foundationBonus,
                revealBonus,
                tableauMoveBonus,
                wasteToTableauBonus,
                kingBonus,
                stockPenalty,
                stockPenaltyImmediate,
                recyclePenalty,
                foundationToTableauPenalty
        );
    }
}