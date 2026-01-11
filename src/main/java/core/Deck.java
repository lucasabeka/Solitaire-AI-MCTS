package core;

import java.util.Collections;
import java.util.Random;
import java.util.Stack;

public class Deck {
    private final Stack<Card> cards;

    public Deck() {
        cards = new Stack<>();
        initialize();
    }

    // Remplit le paquet avec 52 cartes ordonnées
    private void initialize() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                // Par défaut, les cartes sont face cachée dans la pioche
                cards.push(new Card(suit, rank, false));
            }
        }
    }

    // Mélange aléatoire standard
    public void shuffle() {
        Collections.shuffle(cards);
    }

    // Mélange déterministe (Pour les tests et l'IA)
    public void shuffleWithSeed(long seed) {
        Collections.shuffle(cards, new Random(seed));
    }

    public Card draw() {
        if (cards.isEmpty()) return null;
        return cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }
}