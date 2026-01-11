package core;

import java.util.Stack;

public class Tableau extends Pile {

    // Récupère toutes les cartes faces visibles (pour calculer les séquences à bouger)
    public Stack<Card> getVisibleCards() {
        Stack<Card> visible = new Stack<>();
        for (Card card : cards) {
            // --- PROTECTION ANTI-CRASH ---
            if (card == null) continue;
            // -----------------------------
            if (card.isFaceUp()) {
                visible.push(card);
            } else {
                visible.clear(); // On ne peut prendre que les dernières cartes visibles
            }
        }
        return visible;
    }

    // Récupère une sous-pile de 'n' cartes (du haut vers le bas)
    public Stack<Card> getTopCards(int count) {
        Stack<Card> result = new Stack<>();
        // On prend les dernières cartes de la pile principale
        for (int i = 0; i < count; i++) {
            // Note: get(size - 1 - i) serait plus efficace avec une List,
            // mais avec Stack on fait simple pour l'instant.
            result.push(cards.get(cards.size() - count + i));
        }
        return result;
    }

    @Override
    public boolean canAddCard(Card card) {
        // Règle 1 : Si vide, il faut un Roi
        if (isEmpty()) {
            return card.getRank() == 13;
        }

        Card topCard = peekTopCard();

        // Règle 2 : Couleurs alternées (Rouge != Rouge)
        boolean differentColor = card.isRed() != topCard.isRed();

        // Règle 3 : Rang décroissant (5 sur 6)
        boolean correctRank = card.getRank() == topCard.getRank() - 1;

        return differentColor && correctRank;
    }

    @Override
    public Tableau clone() {
        return (Tableau) super.clone();
    }
}
