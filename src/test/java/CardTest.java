import core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testValidConstructor() {
        Card c = new Card(Suit.HEARTS, 1, true);
        assertEquals(Suit.HEARTS, c.getSuit());
        assertEquals(1, c.getRank());
        assertTrue(c.isFaceUp());
        assertTrue(c.isRed()); // Vérifie la délégation à Suit
    }

    @Test
    void testInvalidRankLow() {
        // Vérifie que l'exception est bien levée pour rank < 1
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Card(Suit.SPADES, 0, true);
        });
        assertEquals("Le rang doit être entre 1 et 13", thrown.getMessage()); // Ou le message que tu as mis
    }

    @Test
    void testInvalidRankHigh() {
        // Vérifie que l'exception est bien levée pour rank > 13
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(Suit.SPADES, 14, true);
        });
    }

    @Test
    void testCopyConstructor() {
        Card original = new Card(Suit.CLUBS, 10, false);
        Card copy = new Card(original);

        assertNotSame(original, copy); // Ce sont deux objets différents en mémoire
        assertEquals(original, copy);  // Mais ils ont le même contenu
    }

    @Test
    void testFlip() {
        Card c = new Card(Suit.DIAMONDS, 5, false);
        assertFalse(c.isFaceUp());

        c.flip();
        assertTrue(c.isFaceUp());

        c.flip();
        assertFalse(c.isFaceUp());
    }

    @Test
    void testToString() {
        // Cas 1 : Face cachée
        Card hidden = new Card(Suit.SPADES, 1, false);
        assertEquals("[X]", hidden.toString()); // Ou "Facedown card" selon ton implémentation

        // Cas 2 : Face visible
        Card visible = new Card(Suit.SPADES, 13, true); // Roi de Pique
        // Vérifie que la chaîne contient bien "K" et "SPADES"
        String result = visible.toString();
        assertTrue(result.contains("K"));
        assertTrue(result.contains("SPADES"));
    }

    @Test
    void testEqualsAndHashCode() {
        Card c1 = new Card(Suit.HEARTS, 10, true);
        Card c2 = new Card(Suit.HEARTS, 10, true);
        Card c3 = new Card(Suit.SPADES, 10, true); // Différent par la suite
        Card c4 = new Card(Suit.HEARTS, 9, true);  // Différent par le rang
        Card c5 = new Card(Suit.HEARTS, 10, false); // Différent par faceUp

        // Test de référence (this == obj)
        assertEquals(c1, c1);

        // Test égalité de contenu
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        // Test inégalité
        assertNotEquals(null, c1); // Null check
        assertNotEquals("Une String", c1); // Class check
        assertNotEquals(c1, c3);
        assertNotEquals(c1, c4);
        assertNotEquals(c1, c5);
    }

    @Test
    void testClone() {
        Card original = new Card(Suit.CLUBS, 2, true);
        Card cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original, cloned);
    }

    @Test
    void testEquals_FullBranchCoverage() {
        Card card = new Card(Suit.HEARTS, 10, true);

        // 1. Test de l'identité (if (this == obj))
        // Le test s'arrête dès la première ligne
        assertTrue(card.equals(card));

        // 2. Test du Null (if (obj == null))
        assertFalse(card.equals(null));

        // 3. Test de Classe différente (if (getClass() != obj.getClass()))
        assertFalse(card.equals("Je ne suis pas une carte"));

        // 4. Test Rang différent (rank == card.rank)
        // C'est le premier échec de la dernière ligne
        Card wrongRank = new Card(Suit.HEARTS, 9, true);
        assertFalse(card.equals(wrongRank));

        // 5. Test Couleur différente (suit == card.suit)
        // Ici, le rang est bon, donc Java est obligé d'évaluer la suite... et échoue sur la couleur
        Card wrongSuit = new Card(Suit.SPADES, 10, true);
        assertFalse(card.equals(wrongSuit));

        // 6. Test FaceUp différent (faceUp == card.faceUp)
        // Rang OK, Couleur OK... Java est obligé d'aller jusqu'au bout pour voir que faceUp est faux
        Card wrongFace = new Card(Suit.HEARTS, 10, false);
        assertFalse(card.equals(wrongFace));

        // 7. Tout est identique
        Card copy = new Card(Suit.HEARTS, 10, true);
        assertTrue(card.equals(copy));
    }
}