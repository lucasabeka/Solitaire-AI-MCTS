package core;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Board implements Cloneable {
    private Stack<Card> stock;
    private Stack<Card> waste;
    private List<Foundation> foundations;
    private List<Tableau> tableaux;

    public Board() {
        stock = new Stack<>();
        waste = new Stack<>();
        foundations = new ArrayList<>();
        tableaux = new ArrayList<>();
        for (Suit suit : Suit.values()) foundations.add(new Foundation(suit));
        for (int i = 0; i < 7; i++) tableaux.add(new Tableau());
    }

    public void newGame(long seed) {
        Deck deck = new Deck();
        deck.shuffleWithSeed(seed);
        stock.clear();
        waste.clear();
        foundations.forEach(Pile::clear);
        tableaux.forEach(Pile::clear);

        for (int i = 0; i < 7; i++) {
            Tableau tableau = tableaux.get(i);
            for (int j = 0; j <= i; j++) {
                Card card = deck.draw();
                if (j == i) card.flip();
                tableau.addCard(card);
            }
        }
        while (!deck.isEmpty()) stock.push(deck.draw());
    }

    // --- 1. GÉNÉRATION DES COUPS VALIDES ---

    public List<Move> getValidMoves() {
        List<Move> moves = new ArrayList<>();

        // A. Piocher ou Recycler
        if (!stock.isEmpty()) {
            moves.add(new Move(Move.MoveType.STOCK_TO_WASTE, null, -1, -1));
        } else if (!waste.isEmpty()) {
            moves.add(new Move(Move.MoveType.RECYCLE_WASTE, null, -1, -1));
        }

        // B. Défausse vers Tableau ou Fondation
        if (!waste.isEmpty()) {
            Card wasteCard = waste.peek();

            // Vers Fondation ?
            for (int i = 0; i < 4; i++) {
                if (foundations.get(i).canAddCard(wasteCard))
                    moves.add(new Move(Move.MoveType.WASTE_TO_FOUNDATION, wasteCard, -1, i));
            }
            // Vers Tableau ?
            for (int i = 0; i < 7; i++) {
                if (tableaux.get(i).canAddCard(wasteCard))
                    moves.add(new Move(Move.MoveType.WASTE_TO_TABLEAU, wasteCard, -1, i));
            }
        }

        // C. Tableau vers ...
        for (int srcIdx = 0; srcIdx < 7; srcIdx++) {
            Tableau srcTableau = tableaux.get(srcIdx);
            if (srcTableau.isEmpty()) continue;

            // 1. Vers Fondation (seulement la carte du dessus)
            Card topCard = srcTableau.peekTopCard();
            if (topCard.isFaceUp()) {
                for (int fIdx = 0; fIdx < 4; fIdx++) {
                    if (foundations.get(fIdx).canAddCard(topCard)) {
                        moves.add(new Move(Move.MoveType.TABLEAU_TO_FOUNDATION, topCard, srcIdx, fIdx));
                    }
                }
            }

            // 2. Vers un autre Tableau (Séquences)
            Stack<Card> visibleCards = srcTableau.getVisibleCards();
            // On teste toutes les sous-séquences possibles (ex: Roi, Roi-Dame, Roi-Dame-Valet...)
            for (int len = 1; len <= visibleCards.size(); len++) {
                // La carte à la base de la séquence qu'on veut bouger
                Card bottomCardOfSequence = visibleCards.get(visibleCards.size() - len);

                for (int destIdx = 0; destIdx < 7; destIdx++) {
                    if (srcIdx == destIdx) continue; // Pas sur soi-même

                    if (tableaux.get(destIdx).canAddCard(bottomCardOfSequence)) {
                        moves.add(new Move(Move.MoveType.TABLEAU_TO_TABLEAU, bottomCardOfSequence, srcIdx, destIdx)
                                .setSequenceLength(len));
                    }
                }
            }
        }

        // D. Fondation vers Tableau (Stratégique, parfois utile)
        for (int fIdx = 0; fIdx < 4; fIdx++) {
            Foundation f = foundations.get(fIdx);
            if (!f.isEmpty()) {
                Card card = f.peekTopCard();
                for (int tIdx = 0; tIdx < 7; tIdx++) {
                    if (tableaux.get(tIdx).canAddCard(card)) {
                        moves.add(new Move(Move.MoveType.FOUNDATION_TO_TABLEAU, card, fIdx, tIdx));
                    }
                }
            }
        }

        return moves;
    }

    // --- 2. EXÉCUTION DES COUPS ---

    public void applyMove(Move move) {
        switch (move.getType()) {
            case STOCK_TO_WASTE -> {
                Card c = stock.pop();
                c.flip();
                waste.push(c);
            }
            case RECYCLE_WASTE -> {
                while (!waste.isEmpty()) {
                    Card c = waste.pop();
                    c.flip();
                    stock.push(c);
                }
            }
            case WASTE_TO_FOUNDATION -> foundations.get(move.getTargetIndex()).addCard(waste.pop());
            case WASTE_TO_TABLEAU -> tableaux.get(move.getTargetIndex()).addCard(waste.pop());
            case TABLEAU_TO_FOUNDATION -> {
                Tableau t = tableaux.get(move.getSourceIndex());
                foundations.get(move.getTargetIndex()).addCard(t.removeTopCard());
                revealNextCard(t);
            }
            case TABLEAU_TO_TABLEAU -> {
                Tableau src = tableaux.get(move.getSourceIndex());
                Tableau dest = tableaux.get(move.getTargetIndex());

                // On récupère les cartes à bouger (attention à l'ordre !)
                Stack<Card> toMove = new Stack<>();
                for(int i=0; i < move.getSequenceLength(); i++) {
                    toMove.push(src.removeTopCard());
                }
                // On les remet dans le bon ordre sur la destination
                while(!toMove.isEmpty()) {
                    dest.addCard(toMove.pop());
                }
                revealNextCard(src);
            }
            case FOUNDATION_TO_TABLEAU -> {
                Card c = foundations.get(move.getSourceIndex()).removeTopCard();
                tableaux.get(move.getTargetIndex()).addCard(c);
            }
        }
    }

    // Si on vide une colonne et qu'il reste une carte cachée en dessous, on la retourne
    private void revealNextCard(Tableau t) {
        if (!t.isEmpty() && !t.peekTopCard().isFaceUp()) {
            t.peekTopCard().flip();
        }
    }

    // --- 3. MÉTHODES UTILITAIRES ---

    public boolean isGameWon() {
        return foundations.stream().allMatch(Foundation::isComplete);
    }

    public long getGameStateHash() {
        long result = 1;
        for (Tableau t : tableaux) {
            result = 31 * result + t.size();
            if (!t.isEmpty()) result = 31 * result + t.peekTopCard().hashCode();
        }
        for (Foundation f : foundations) {
            result = 31 * result + (f.isEmpty() ? 0 : f.peekTopCard().hashCode());
        }
        result = 31 * result + stock.size();
        if (!waste.isEmpty()) result = 31 * result + waste.peek().hashCode();
        return result;
    }

    @Override
    public Board clone() {
        try {
            Board cloned = (Board) super.clone();

            // CORRECTION : Copie profonde carte par carte pour Stock et Waste
            cloned.stock = new Stack<>();
            for(Card c : this.stock) cloned.stock.push(c.clone());

            cloned.waste = new Stack<>();
            for(Card c : this.waste) cloned.waste.push(c.clone());

            cloned.foundations = new ArrayList<>();
            for (Foundation f : this.foundations) cloned.foundations.add(f.clone());

            cloned.tableaux = new ArrayList<>();
            for (Tableau t : this.tableaux) cloned.tableaux.add(t.clone());

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters
    public Stack<Card> getStock() { return stock; }
    public Stack<Card> getWaste() { return waste; }
    public List<Foundation> getFoundations() { return foundations; }
    public List<Tableau> getTableaux() { return tableaux; }
}