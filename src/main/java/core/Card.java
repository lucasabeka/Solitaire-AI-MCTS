package core;

import java.util.Objects;

public class Card {
    private final Suit suit;
    private final int rank; // 1 (As) à 13 (Roi)
    private boolean faceUp; // Est-ce que la carte est visible ?

    public Card(Suit suit, int rank, boolean faceUp) {
        if (rank < 1 || rank > 13)
            throw new IllegalArgumentException("Le rang doit être entre 1 et 13");
        this.suit = suit;
        this.rank = rank;
        this.faceUp = faceUp;
    }

    // Constructeur de copie (très important pour l'IA plus tard)
    public Card(Card other) {
        this.suit = other.suit;
        this.rank = other.rank;
        this.faceUp = other.faceUp;
    }

    public void flip() {
        faceUp = !faceUp;
    }

    public Suit getSuit() { return suit; }
    public int getRank() { return rank; }
    public boolean isFaceUp() { return faceUp; }
    public boolean isRed() { return suit.isRed(); }

    @Override
    public String toString() {
        String[] names = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        return (faceUp ? names[rank] + " of " + suit : "[X]");
    }

    // Nécessaire pour comparer les cartes correctement
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return rank == card.rank && suit == card.suit && faceUp == card.faceUp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank, faceUp);
    }

    @Override
    public Card clone() {
        return new Card(this);
    }

}
