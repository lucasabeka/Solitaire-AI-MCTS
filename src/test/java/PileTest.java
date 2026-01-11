import core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PileTest {

    // Nous utilisons Tableau comme implémentation concrète pour tester la classe mère Pile
    @Test
    void testStackOperations() {
        Pile pile = new Tableau();
        assertTrue(pile.isEmpty());
        assertEquals(0, pile.size());

        // Test Peek/Remove sur pile vide (Branche : if(isEmpty) return null)
        assertNull(pile.peekTopCard());
        assertNull(pile.removeTopCard());

        // Test Ajout
        Card c1 = new Card(Suit.HEARTS, 10, true);
        pile.addCard(c1);

        assertFalse(pile.isEmpty());
        assertEquals(1, pile.size());
        assertEquals(c1, pile.peekTopCard());
        assertEquals(c1, pile.getCards().peek());

        // Test Suppression
        Card removed = pile.removeTopCard();
        assertEquals(c1, removed);
        assertTrue(pile.isEmpty());
    }

    @Test
    void testClear() {
        Pile pile = new Tableau();
        pile.addCard(new Card(Suit.HEARTS, 1, true));
        pile.clear();
        assertTrue(pile.isEmpty());
    }

    @Test
    void testCloneDeepCopy() {
        Pile original = new Tableau();
        original.addCard(new Card(Suit.SPADES, 1, true));

        // Clonage
        Pile copy = original.clone();

        // Vérifications de base
        assertNotSame(original, copy); // Objets différents
        assertEquals(1, copy.size());

        // Vérification du clonage profond (Deep Copy)
        // Si je modifie la copie, l'original ne doit PAS changer
        copy.removeTopCard();
        assertTrue(copy.isEmpty());
        assertFalse(original.isEmpty()); // L'original a toujours sa carte
    }
}