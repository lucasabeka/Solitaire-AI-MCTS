import core.*;
import ia.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MCTSSolverTest {

    @Test
    void testFindBestMove_Instinct_Foundation() {
        // Teste la Phase 1 (Instinct) : Monter une carte en fondation
        Board board = new Board();
        board.getTableaux().forEach(Pile::clear);
        board.getStock().clear();
        board.getWaste().clear();

        // Setup : As de Pique disponible
        Card ace = new Card(Suit.SPADES, 1, true);
        board.getTableaux().get(0).addCard(ace);

        MCTSSolver solver = new MCTSSolver();
        Move bestMove = solver.findBestMove(board);

        assertNotNull(bestMove);
        assertEquals(Move.MoveType.TABLEAU_TO_FOUNDATION, bestMove.getType());
        assertEquals(1, bestMove.getCard().getRank());
    }

    @Test
    void testHeuristicsScoring() {
        MCTSSolver solver = new MCTSSolver();
        Board board = new Board(); // Plateau vide par défaut

        Card ace = new Card(Suit.SPADES, 1, true);
        Card king = new Card(Suit.HEARTS, 13, true);
        Card two = new Card(Suit.CLUBS, 2, true);

        // 1. Test TABLEAU_TO_FOUNDATION (+1000)
        Move m1 = new Move(Move.MoveType.TABLEAU_TO_FOUNDATION, ace, 0, 0);
        assertEquals(1000, solver.evaluateMoveImmediateValue(board, m1));

        // 2. Test WASTE_TO_FOUNDATION (+1000)
        Move m2 = new Move(Move.MoveType.WASTE_TO_FOUNDATION, ace, -1, 0);
        assertEquals(1000, solver.evaluateMoveImmediateValue(board, m2));

        // 3. Test TABLEAU_TO_TABLEAU standard (+50)
        Move m3 = new Move(Move.MoveType.TABLEAU_TO_TABLEAU, two, 1, 2);
        // Pas de bonus spécial ici (plateau vide, pas de carte cachée dessous)
        assertEquals(50, solver.evaluateMoveImmediateValue(board, m3));

        // 4. Test WASTE_TO_TABLEAU (+70)
        Move m4 = new Move(Move.MoveType.WASTE_TO_TABLEAU, king, -1, 1);
        assertEquals(70, solver.evaluateMoveImmediateValue(board, m4));

        // 5. Test STOCK_TO_WASTE (-10)
        Move mStock = new Move(Move.MoveType.STOCK_TO_WASTE, null, -1, -1);
        assertEquals(-10, solver.evaluateMoveImmediateValue(board, mStock));

        // 6. Test FOUNDATION_TO_TABLEAU (-inf)
        Move mDown = new Move(Move.MoveType.FOUNDATION_TO_TABLEAU, two, 0, 1);
        assertEquals(Integer.MIN_VALUE, solver.evaluateMoveImmediateValue(board, mDown));
    }

    @Test
    void testCalculateScore() {
        MCTSSolver solver = new MCTSSolver();
        Board board = new Board();
        board.getTableaux().forEach(Pile::clear);

        // 1. Cas normal : 2 cartes en fondation, 1 carte visible sur tableau
        board.getFoundations().get(0).addCard(new Card(Suit.HEARTS, 1, true));
        board.getFoundations().get(0).addCard(new Card(Suit.HEARTS, 2, true));

        board.getTableaux().get(0).addCard(new Card(Suit.SPADES, 5, true));

        // Score attendu : (2 * 500) + (1 * 100) = 1100
        assertEquals(1100, solver.calculateScore(board));

        // 2. Cas Victoire : Bonus de 100000
        board.getFoundations().forEach(Pile::clear);
        for (Foundation f : board.getFoundations()) {
            f.addCard(new Card(f.getSuit(), 13, true));
        }
        assertEquals(100000, solver.calculateScore(board));
    }
}