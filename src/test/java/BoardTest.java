import core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void testNewGameSetup() {
        Board board = new Board();
        board.newGame(123L);

        // Vérification des tableaux
        assertEquals(1, board.getTableaux().get(0).size()); // 1 carte
        assertEquals(7, board.getTableaux().get(6).size()); // 7 cartes
        assertTrue(board.getTableaux().get(0).peekTopCard().isFaceUp()); // La dernière doit être visible

        // Vérification du Stock
        // Total 52 - (1+2+3+4+5+6+7 = 28 cartes sur table) = 24 cartes restantes
        assertEquals(24, board.getStock().size());

        // Vérification Waste et Fondations
        assertTrue(board.getWaste().isEmpty());
        for (Foundation f : board.getFoundations()) {
            assertTrue(f.isEmpty());
        }
    }

    @Test
    void testClone() {
        Board original = new Board();
        original.newGame(123L);

        Board copy = original.clone();

        // Modifions la copie
        copy.getStock().pop();

        // L'original ne doit pas avoir changé
        assertEquals(24, original.getStock().size());
        assertEquals(23, copy.getStock().size());

        // Vérifions que les tableaux sont bien des instances différentes
        assertNotSame(original.getTableaux().get(0), copy.getTableaux().get(0));
    }

    @Test
    void testGameWon() {
        Board board = new Board();
        assertFalse(board.isGameWon());

        // Hack: Remplissons manuellement les fondations pour tester la victoire
        // (En pratique, il faudrait mocker ou utiliser la méthode add, mais on teste juste isGameWon)
        // Ceci suppose que tes tests peuvent accéder aux méthodes package-private ou public
    }

    @Test
    void testHashStability() {
        Board b1 = new Board();
        b1.newGame(555L);

        Board b2 = new Board();
        b2.newGame(555L);

        assertEquals(b1.getGameStateHash(), b2.getGameStateHash());
    }

    @Test
    void testGameStateHash_FullCoverage() {
        Board board = new Board();
        board.newGame(123L);

        // Cas 1 : État initial (Waste vide, Fondations vides, Tableaux pleins)
        // Cela couvre les branches "else" ou "false" des conditions
        long initialHash = board.getGameStateHash();
        assertNotEquals(0, initialHash);

        // Cas 2 : Forcer les branches inverses

        // A. Remplir le Waste (pour entrer dans if (!waste.isEmpty()))
        if (!board.getStock().isEmpty()) {
            Card c = board.getStock().pop();
            c.flip(); // Important car le hash utilise souvent le hash de la carte visible
            board.getWaste().push(c);
        }

        // B. Vider un Tableau (pour ne PAS entrer dans if (!t.isEmpty()))
        board.getTableaux().get(0).clear();

        // C. Remplir une Fondation (pour le ternaire f.isEmpty() ? 0 : code)
        board.getFoundations().get(0).addCard(new Card(Suit.HEARTS, 1, true));

        // Recalculer le hash avec ces nouvelles conditions
        long newHash = board.getGameStateHash();

        assertNotEquals(initialHash, newHash);
    }

    @Test
    void testIsGameWon_True() {
        Board board = new Board();
        // Par défaut, board vide = pas gagné
        assertFalse(board.isGameWon());

        // On "triche" pour simuler une victoire instantanée
        // On remplit les 4 fondations avec un Roi (ce qui suffit pour isComplete)
        for (Foundation f : board.getFoundations()) {
            // Note: On met juste un Roi, car Foundation.isComplete vérifie juste le sommet
            f.addCard(new Card(f.getSuit(), 13, true));
        }

        assertTrue(board.isGameWon(), "Devrait retourner true si toutes les fondations ont un Roi");
    }
}
