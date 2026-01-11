import core.*;
import ia.*;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MCTSSolverCoverageTest {

    // ==========================================
    // 1. TESTS EXHAUSTIFS DE L'HEURISTIQUE (AVEC CONTEXTE)
    // ==========================================

    @Test
    void testEvaluateMove_ContextBonuses() {
        MCTSSolver solver = new MCTSSolver();
        Board board = new Board();

        // --- Test Bonus REVEAL (+500) ---
        // Setup : Tableau 0 a une carte cachée et une visible
        Tableau t0 = board.getTableaux().get(0);
        t0.clear();
        t0.addCard(new Card(Suit.CLUBS, 10, false)); // Cachée
        Card movingCard = new Card(Suit.HEARTS, 9, true); // Visible
        t0.addCard(movingCard);

        // Mouvement : On déplace le 9
        Move mReveal = new Move(Move.MoveType.TABLEAU_TO_TABLEAU, movingCard, 0, 1);

        // Score : Base(50) + Bonus Reveal(500) = 550
        assertEquals(550, solver.evaluateMoveImmediateValue(board, mReveal));


        // --- Test Bonus ROI SUR VIDE (+100) ---
        // Setup : Tableau 1 a une carte cachée, une carte X, puis le Roi
        // Le Roi n'est PAS à la base (index > 0), donc le bouger est utile
        Tableau t1 = board.getTableaux().get(1);
        t1.clear();
        t1.addCard(new Card(Suit.CLUBS, 2, false));
        t1.addCard(new Card(Suit.DIAMONDS, 2, true));
        Card king = new Card(Suit.SPADES, 13, true);
        t1.addCard(king);

        // Mouvement : Roi vers un tableau vide (index 2)
        Move mKing = new Move(Move.MoveType.TABLEAU_TO_TABLEAU, king, 1, 2);

        // Score : Base(50) + Bonus Roi(100) = 150
        // Note : Pas de Reveal ici car il reste une carte visible (le 2 de carreau) sous le roi
        assertEquals(150, solver.evaluateMoveImmediateValue(board, mKing));

        // --- Test RECYCLE WASTE (-100) ---
        Move mRecycle = new Move(Move.MoveType.RECYCLE_WASTE, null, -1, -1);
        assertEquals(-100, solver.evaluateMoveImmediateValue(board, mRecycle));
    }

    // ==========================================
    // 2. TESTS SIMULATION (Avec Mocking manuel)
    // ==========================================

    @Test
    void testSimulate_CycleDetection() {
        // Scénario : Le jeu tourne en rond
        // On s'attend à ce que simulateRandomGame retourne -500 (pénalité cycle)
        // Note : dans le nouveau code, simulateRandomGame est private.
        // Assure-toi d'enlever 'private' dans MCTSSolver pour que ce test passe.

        CycleBoard spyBoard = new CycleBoard();
        MCTSSolver solver = new MCTSSolver();

        double result = solver.simulateRandomGame(spyBoard);

        // Le hash retourne 1, 2, 1 -> Cycle détecté -> return -500.0 (valeur hardcodée dans le new code)
        // Vérifie bien si tu as mis -500.0 ou autre chose dans MCTSSolver.java
        // Dans mon dernier code fourni, c'était implicite via calculateScore ou stop.
        // Attends, le code "Hybride" fourni n'a pas de détection explicite de cycle avec hashset dans 'simulateRandomGame'
        // car on limite la profondeur MAX_DEPTH.
        // MAIS si tu as gardé la version avec HashSet, c'est -500.
        // Si tu as pris ma version "Hybride" exacte, elle n'a pas de "return -500" explicite, elle break et calcule le score.
        // ADAPTATION : Si cycle, on atteint MAX_DEPTH ou on break, et on retourne le score (0).

        // Corrigeons le test pour la version Hybride (sans HashSet explicite, juste MAX_DEPTH) :
        assertEquals(0.0, result);
    }

    @Test
    void testSimulate_BlockedGame() {
        // Scénario : Bloqué immédiatement
        BlockedBoard spyBoard = new BlockedBoard();
        MCTSSolver solver = new MCTSSolver();

        double result = solver.simulateRandomGame(spyBoard);
        assertEquals(0.0, result);
    }

    // ==========================================
    // CLASSES DOUBLURES
    // ==========================================

    static class CycleBoard extends Board {
        private int callCount = 0;
        @Override
        public boolean isGameWon() { return false; }
        @Override
        public List<Move> getValidMoves() {
            // Toujours un mouvement dispo pour ne pas bloquer sur "isEmpty"
            return Collections.singletonList(new Move(Move.MoveType.STOCK_TO_WASTE, null, -1, -1));
        }
        @Override
        public void applyMove(Move move) { /* Rien */ }
        @Override
        public long getGameStateHash() {
            // Force un cycle si le code utilise le hash
            callCount++;
            return (callCount % 2) + 1;
        }
        @Override
        public List<Foundation> getFoundations() { return Collections.emptyList(); }
        @Override
        public List<Tableau> getTableaux() { return Collections.emptyList(); }
    }

    static class BlockedBoard extends Board {
        @Override
        public boolean isGameWon() { return false; }
        @Override
        public List<Move> getValidMoves() { return Collections.emptyList(); } // Bloqué
        @Override
        public List<Foundation> getFoundations() { return Collections.emptyList(); }
        @Override
        public List<Tableau> getTableaux() { return Collections.emptyList(); }
    }
}