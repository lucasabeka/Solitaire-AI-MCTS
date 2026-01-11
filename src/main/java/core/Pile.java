package core;

import java.util.Stack;

// "abstract" signifie qu'on ne peut pas faire "new Pile()".
// On doit forcément créer une Foundation ou un Tableau.
public abstract class Pile implements Cloneable {
    protected Stack<Card> cards;

    public Pile() {
        this.cards = new Stack<>();
    }

    public void addCard(Card card) {
        cards.push(card);
    }

    public Card removeTopCard() {
        if (isEmpty()) return null;
        return cards.pop();
    }

    public Card peekTopCard() {
        if (isEmpty()) return null;
        return cards.peek();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public void clear() {
        cards.clear();
    }

    public Stack<Card> getCards() {
        return cards;
    }

    // Chaque enfant devra définir ses propres règles
    public abstract boolean canAddCard(Card card);

    // CRUCIAL POUR L'IA : Permet de créer une copie parfaite de la pile
    @Override
    public Pile clone() {
        try {
            Pile cloned = (Pile) super.clone();
            // CORRECTION : On ne fait pas juste cards.clone(), on recrée une nouvelle Stack
            // et on clone CHAQUE carte individuellement.
            cloned.cards = new Stack<>();
            for (Card c : this.cards) {
                cloned.cards.push(c.clone()); // Utilise le clone() de Card
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
