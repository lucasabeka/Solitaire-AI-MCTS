package core;

public class Move {
    public enum MoveType {
        STOCK_TO_WASTE,        // Piocher
        WASTE_TO_TABLEAU,      // Défausse -> Tableau
        WASTE_TO_FOUNDATION,   // Défausse -> Fondation
        TABLEAU_TO_TABLEAU,    // Déplacer une colonne
        TABLEAU_TO_FOUNDATION, // Monter une carte
        FOUNDATION_TO_TABLEAU, // Redescendre une carte
        RECYCLE_WASTE          // Retourner la défausse quand la pioche est vide
    }

    private final MoveType type;
    private final Card card;       // Quelle carte est bougée (peut être null pour la pioche)
    private final int sourceIndex; // D'où elle vient (ex: index du tableau 0-6)
    private final int targetIndex; // Où elle va
    private int sequenceLength = 1; // Combien de cartes on bouge (ex: déplacer Roi + Dame + Valet = 3)

    public Move(MoveType type, Card card, int sourceIndex, int targetIndex) {
        this.type = type;
        this.card = card;
        this.sourceIndex = sourceIndex;
        this.targetIndex = targetIndex;
    }

    public Move setSequenceLength(int length) {
        this.sequenceLength = length;
        return this;
    }

    public MoveType getType() { return type; }
    public Card getCard() { return card; }
    public int getSourceIndex() { return sourceIndex; }
    public int getTargetIndex() { return targetIndex; }
    public int getSequenceLength() { return sequenceLength; }

    @Override
    public String toString() {
        return String.format("%s: %s (De %d vers %d)", type, card, sourceIndex, targetIndex);
    }
}
