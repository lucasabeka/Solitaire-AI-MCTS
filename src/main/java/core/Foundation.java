package core;

public class Foundation extends Pile {
    private final Suit suit;

    public Foundation(Suit suit) {
        super(); // Appelle le constructeur de Pile (crée le Stack)
        this.suit = suit;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public boolean canAddCard(Card card) {
        // Règle 1 : Bonne couleur ?
        if (card.getSuit() != suit) {
            return false;
        }

        // Règle 2 : Si vide, il faut un As
        if (isEmpty()) {
            return card.getRank() == 1;
        }

        // Règle 3 : Sinon, rang supérieur (3 sur 2)
        return card.getRank() == peekTopCard().getRank() + 1;
    }

    public boolean isComplete() {
        return !isEmpty() && peekTopCard().getRank() == 13;
    }

    @Override
    public Foundation clone() {
        return (Foundation) super.clone();
    }


}