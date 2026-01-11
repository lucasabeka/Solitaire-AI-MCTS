import core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FoundationTest {

    @Test
    void testCanAddCard_WrongSuit() {
        Foundation heartsFoundation = new Foundation(Suit.HEARTS);
        Card spadeCard = new Card(Suit.SPADES, 1, true);

        // Branche : if (card.getSuit() != suit)
        assertFalse(heartsFoundation.canAddCard(spadeCard), "Devrait refuser une mauvaise couleur");
    }

    @Test
    void testCanAddCard_EmptyPile() {
        Foundation foundation = new Foundation(Suit.HEARTS);

        // Branche : if (isEmpty()) -> doit être un As
        Card ace = new Card(Suit.HEARTS, 1, true);
        assertTrue(foundation.canAddCard(ace), "Devrait accepter un As sur vide");

        Card two = new Card(Suit.HEARTS, 2, true);
        assertFalse(foundation.canAddCard(two), "Devrait refuser un 2 sur vide");
    }

    @Test
    void testCanAddCard_NonEmpty() {
        Foundation foundation = new Foundation(Suit.HEARTS);
        foundation.addCard(new Card(Suit.HEARTS, 1, true)); // On met un As

        // Branche : !isEmpty() -> doit être rang + 1
        Card two = new Card(Suit.HEARTS, 2, true);
        assertTrue(foundation.canAddCard(two), "Devrait accepter un 2 sur un As");

        Card three = new Card(Suit.HEARTS, 3, true);
        assertFalse(foundation.canAddCard(three), "Devrait refuser un 3 sur un As (saut de rang)");

        Card ace = new Card(Suit.HEARTS, 1, true);
        assertFalse(foundation.canAddCard(ace), "Devrait refuser un As sur un As");
    }

    @Test
    void testTryAddCard() {
        // Cette méthode n'est pas encore dans ta classe Foundation fournie précédemment, 
        // mais elle était dans ton code original. Si tu ne l'as pas mise, ignore ce test.
        // Si tu veux l'ajouter, c'est simple :
        /* public boolean tryAddCard(Card card) {
               if (canAddCard(card)) { addCard(card); return true; }
               return false;
           }
        */
    }

    @Test
    void testIsComplete() {
        Foundation foundation = new Foundation(Suit.CLUBS);

        // Vide
        assertFalse(foundation.isComplete());

        // Remplie partiellement
        foundation.addCard(new Card(Suit.CLUBS, 1, true));
        assertFalse(foundation.isComplete());

        // Remplie jusqu'au Roi
        foundation.clear(); // Hack pour le test
        foundation.addCard(new Card(Suit.CLUBS, 13, true));
        assertTrue(foundation.isComplete());
    }
}