import core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void testMoveCreationAndGetters() {
        Card card = new Card(Suit.HEARTS, 1, true);
        Move move = new Move(Move.MoveType.TABLEAU_TO_FOUNDATION, card, 0, 1);

        assertEquals(Move.MoveType.TABLEAU_TO_FOUNDATION, move.getType());
        assertEquals(card, move.getCard());
        assertEquals(0, move.getSourceIndex());
        assertEquals(1, move.getTargetIndex());
        assertEquals(1, move.getSequenceLength()); // Valeur par défaut
    }

    @Test
    void testSetSequenceLength() {
        Move move = new Move(Move.MoveType.TABLEAU_TO_TABLEAU, null, 1, 2);

        // Test du pattern "Builder" (la méthode doit retourner l'objet lui-même)
        Move result = move.setSequenceLength(5);

        assertSame(move, result);
        assertEquals(5, move.getSequenceLength());
    }

    @Test
    void testToString() {
        Card card = new Card(Suit.CLUBS, 10, true);
        Move move = new Move(Move.MoveType.WASTE_TO_TABLEAU, card, -1, 2);

        String str = move.toString();
        assertNotNull(str);
        // On vérifie juste que le string contient les infos essentielles
        assertTrue(str.contains("WASTE_TO_TABLEAU"));
        assertTrue(str.contains("2")); // Target index
    }
}