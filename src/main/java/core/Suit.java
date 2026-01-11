package core;

public enum Suit {
    CLUBS, // Trèfle
    DIAMONDS, // Carreau
    HEARTS, // Cœur
    SPADES; // Pique

    // Utile pour savoir si on peut alterner les couleurs dans le tableau
    public boolean isRed() {
        return this == DIAMONDS || this == HEARTS;
    }

    public boolean isBlack() {
        return this == CLUBS || this == SPADES;
    }
}
