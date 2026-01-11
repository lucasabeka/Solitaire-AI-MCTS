import core.Suit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SuitTest {

    @Test
    void testIsRed() {
        // Branche 1 : Les rouges
        assertTrue(Suit.DIAMONDS.isRed());
        assertTrue(Suit.HEARTS.isRed());

        // Branche 2 : Les noirs (ne doivent pas être rouges)
        assertFalse(Suit.CLUBS.isRed());
        assertFalse(Suit.SPADES.isRed());
    }

    @Test
    void testIsBlack() {
        // Branche 1 : Les noirs
        assertTrue(Suit.CLUBS.isBlack());
        assertTrue(Suit.SPADES.isBlack());

        // Branche 2 : Les rouges (ne doivent pas être noirs)
        assertFalse(Suit.DIAMONDS.isBlack());
        assertFalse(Suit.HEARTS.isBlack());
    }
}