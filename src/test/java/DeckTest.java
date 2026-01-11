import core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void testInitialization() {
        Deck deck = new Deck();
        assertEquals(52, deck.size());
        assertFalse(deck.isEmpty());
    }

    @Test
    void testShuffleWithSeed() {
        Deck deck1 = new Deck();
        deck1.shuffleWithSeed(12345L);

        Deck deck2 = new Deck();
        deck2.shuffleWithSeed(12345L); // Même graine

        // Les deux paquets doivent être identiques
        for (int i = 0; i < 52; i++) {
            assertEquals(deck1.draw(), deck2.draw());
        }
    }

    @Test
    void testDraw() {
        Deck deck = new Deck();
        Card c = deck.draw();
        assertNotNull(c);
        assertEquals(51, deck.size());

        // Vider le deck
        while(!deck.isEmpty()) {
            deck.draw();
        }
        assertNull(deck.draw()); // Doit retourner null quand vide
    }
}
