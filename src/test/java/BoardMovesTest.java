import core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BoardMovesTest {
    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(); // Plateau vide pour commencer
    }

    @Test
    void testStockMoves() {
        // Cas 1 : Piocher
        board.getStock().push(new Card(Suit.HEARTS, 5, false));
        List<Move> moves = board.getValidMoves();

        assertEquals(1, moves.size());
        assertEquals(Move.MoveType.STOCK_TO_WASTE, moves.get(0).getType());

        // Exécution
        board.applyMove(moves.get(0));
        assertTrue(board.getStock().isEmpty());
        assertFalse(board.getWaste().isEmpty());
        assertTrue(board.getWaste().peek().isFaceUp());

        // Cas 2 : Recycler (Stock vide, Waste plein)
        moves = board.getValidMoves();
        assertEquals(1, moves.size());
        assertEquals(Move.MoveType.RECYCLE_WASTE, moves.get(0).getType());

        // Exécution
        board.applyMove(moves.get(0));
        assertFalse(board.getStock().isEmpty());
        assertTrue(board.getWaste().isEmpty());
        assertFalse(board.getStock().peek().isFaceUp()); // Doit être redevenue cachée
    }

    @Test
    void testWasteToFoundation() {
        // Setup : As de Cœur dans la défausse
        Card ace = new Card(Suit.HEARTS, 1, true);
        board.getWaste().push(ace);

        List<Move> moves = board.getValidMoves();

        // On devrait pouvoir l'envoyer vers la fondation Cœur (index 2 dans Suit.values() souvent, ou recherche auto)
        boolean found = moves.stream()
                .anyMatch(m -> m.getType() == Move.MoveType.WASTE_TO_FOUNDATION
                        && m.getCard().equals(ace));
        assertTrue(found);

        // On exécute le premier move valide trouvé
        Move move = moves.stream().filter(m -> m.getType() == Move.MoveType.WASTE_TO_FOUNDATION).findFirst().get();
        board.applyMove(move);

        assertTrue(board.getWaste().isEmpty());
        assertFalse(board.getFoundations().get(move.getTargetIndex()).isEmpty());
    }

    @Test
    void testTableauToTableauSequence() {
        // Setup : 
        // T0 : [Roi Cœur]
        // T1 : [Dame Pique, Valet Cœur]
        Tableau t0 = board.getTableaux().get(0);
        Tableau t1 = board.getTableaux().get(1);

        t0.addCard(new Card(Suit.HEARTS, 13, true)); // Roi
        t1.addCard(new Card(Suit.SPADES, 12, true)); // Dame
        t1.addCard(new Card(Suit.HEARTS, 11, true)); // Valet

        List<Move> moves = board.getValidMoves();

        // On doit trouver un mouvement qui déplace Dame+Valet (Seq=2) vers le Roi
        Move seqMove = moves.stream()
                .filter(m -> m.getType() == Move.MoveType.TABLEAU_TO_TABLEAU
                        && m.getSequenceLength() == 2)
                .findFirst()
                .orElse(null);

        assertNotNull(seqMove);
        assertEquals(1, seqMove.getSourceIndex());
        assertEquals(0, seqMove.getTargetIndex());

        // Exécution
        board.applyMove(seqMove);

        assertEquals(3, t0.size()); // Roi, Dame, Valet
        assertTrue(t1.isEmpty());
    }

    @Test
    void testRevealHiddenCard() {
        // Vérifie qu'en retirant une carte, celle du dessous se retourne
        Tableau t0 = board.getTableaux().get(0);
        t0.addCard(new Card(Suit.CLUBS, 10, false)); // Cachée
        t0.addCard(new Card(Suit.DIAMONDS, 5, true)); // Visible (à bouger)

        // On simule un mouvement vers une fondation (pour faire simple)
        // Mais ici on teste juste applyMove manuellement ou via move généré
        // Créons un move artificiel pour tester la logique applyMove directement
        Move move = new Move(Move.MoveType.TABLEAU_TO_FOUNDATION, null, 0, 1);

        // Hack: On ajoute la carte à la fondation pour que la logique tienne, 
        // ou on teste juste la logique de "remove" dans applyMove
        // Pour faire propre, rendons la fondation valide
        board.getFoundations().get(1).addCard(new Card(Suit.DIAMONDS, 4, true));

        board.applyMove(move);

        // La carte cachée (10 de Trèfle) doit être devenue visible !
        assertTrue(t0.peekTopCard().isFaceUp());
    }

    @Test
    void testFoundationToTableau() {
        Foundation f = board.getFoundations().get(0); // Clubs
        f.addCard(new Card(Suit.CLUBS, 1, true));
        f.addCard(new Card(Suit.CLUBS, 2, true));

        Tableau t = board.getTableaux().get(0);
        t.addCard(new Card(Suit.DIAMONDS, 3, true));

        List<Move> moves = board.getValidMoves();
        boolean canMoveDown = moves.stream()
                .anyMatch(m -> m.getType() == Move.MoveType.FOUNDATION_TO_TABLEAU);

        assertTrue(canMoveDown);

        Move move = moves.stream().filter(m -> m.getType() == Move.MoveType.FOUNDATION_TO_TABLEAU).findFirst().get();
        board.applyMove(move);

        assertEquals(1, f.size()); // Il reste l'As
        assertEquals(2, t.size()); // 3 de Carreau + 2 de Trèfle
    }

    @Test
    void testRevealNextCard_ThreeBranches() {
        // Pour couvrir : if (!t.isEmpty() && !t.peekTopCard().isFaceUp())

        // Branche 1 : La pile devient vide (pas de reveal)
        Tableau tEmpty = board.getTableaux().get(0);
        tEmpty.clear();
        tEmpty.addCard(new Card(Suit.HEARTS, 1, true));
        // Simuler un move qui vide la pile
        // (On triche un peu en appelant directement la logique interne ou via un move valide)
        Move moveEmpty = new Move(Move.MoveType.TABLEAU_TO_FOUNDATION, null, 0, 0);
        // Hack: on rend la fondation compatible
        board.getFoundations().get(0).clear();
        board.applyMove(moveEmpty);
        assertTrue(tEmpty.isEmpty()); // La condition !isEmpty() est fausse

        // Branche 2 : La pile n'est pas vide, mais la carte dessous est DÉJÀ visible (pas de flip)
        Tableau tVisible = board.getTableaux().get(1);
        tVisible.clear();
        tVisible.addCard(new Card(Suit.SPADES, 10, true)); // Déjà visible
        tVisible.addCard(new Card(Suit.HEARTS, 9, true));  // Celle qu'on bouge

        // On bouge le 9 vers une fondation (simulée)
        board.getFoundations().get(2).addCard(new Card(Suit.HEARTS, 8, true));
        Move moveVisible = new Move(Move.MoveType.TABLEAU_TO_FOUNDATION, null, 1, 2);
        board.applyMove(moveVisible);

        assertTrue(tVisible.peekTopCard().isFaceUp()); // Était déjà true, reste true

        // Branche 3 : La carte dessous est cachée (Le cas standard -> flip)
        Tableau tHidden = board.getTableaux().get(2);
        tHidden.clear();
        tHidden.addCard(new Card(Suit.DIAMONDS, 5, false)); // Cachée
        tHidden.addCard(new Card(Suit.CLUBS, 4, true));     // Celle qu'on bouge

        board.getFoundations().get(0).addCard(new Card(Suit.CLUBS, 3, true));
        Move moveHidden = new Move(Move.MoveType.TABLEAU_TO_FOUNDATION, null, 2, 0);
        board.applyMove(moveHidden);

        assertTrue(tHidden.peekTopCard().isFaceUp()); // A dû passer à true
    }

    @Test
    void testGetValidMoves_EmptyTableau_And_FaceDownTop() {
        // Couvrir : if (srcTableau.isEmpty()) continue;
        board.getTableaux().get(0).clear();

        // Couvrir : if (topCard.isFaceUp())
        // (Cas théoriquement impossible en jeu normal mais possible techniquement)
        Tableau t1 = board.getTableaux().get(1);
        t1.clear();
        t1.addCard(new Card(Suit.HEARTS, 10, false)); // Carte face cachée au sommet !

        List<Move> moves = board.getValidMoves();

        // Vérifier que le tableau vide et le tableau face cachée ne génèrent pas d'erreurs
        // et ne proposent pas de moves sortants invalides
        boolean hasMovesFrom0 = moves.stream().anyMatch(m -> m.getSourceIndex() == 0);
        boolean hasMovesFrom1 = moves.stream().anyMatch(m -> m.getSourceIndex() == 1);

        assertFalse(hasMovesFrom0);
        assertFalse(hasMovesFrom1);
    }

    @Test
    void testRecycleWaste_Specific() {
        // Couvrir explicitement le "else if (!waste.isEmpty())" de la pioche
        board.getStock().clear();

        // CORRECTION : On met un 2 (pas un As) pour éviter qu'il puisse aller en fondation
        board.getWaste().add(new Card(Suit.HEARTS, 2, true));

        List<Move> moves = board.getValidMoves();

        // Maintenant, seul le recyclage est possible
        assertEquals(1, moves.size());
        assertEquals(Move.MoveType.RECYCLE_WASTE, moves.get(0).getType());

        board.applyMove(moves.get(0));
        assertEquals(1, board.getStock().size());
        assertTrue(board.getWaste().isEmpty());
    }

    @Test
    void testWasteToTableau() {
        // Setup : On met un Roi dans la défausse
        // Un Roi peut aller sur un tableau vide
        board.getWaste().push(new Card(Suit.HEARTS, 13, true));
        board.getTableaux().get(0).clear(); // On s'assure que le tableau 0 est vide

        List<Move> moves = board.getValidMoves();

        // Vérification que le coup est généré (Couverture de getValidMoves)
        Move move = moves.stream()
                .filter(m -> m.getType() == Move.MoveType.WASTE_TO_TABLEAU)
                .findFirst()
                .orElse(null);

        assertNotNull(move, "Devrait proposer de déplacer le Roi vers le tableau vide");

        // Exécution (Couverture de applyMove -> case WASTE_TO_TABLEAU)
        board.applyMove(move);

        assertTrue(board.getWaste().isEmpty());
        assertEquals(1, board.getTableaux().get(0).size());
        assertEquals(13, board.getTableaux().get(0).peekTopCard().getRank());
    }

    @Test
    void testNoStockNoWaste() {
        // Ce test couvre le cas implicite où ni la pioche ni la défausse ne sont disponibles
        // dans la méthode getValidMoves (les deux 'if' sont faux)
        board.getStock().clear();
        board.getWaste().clear();

        List<Move> moves = board.getValidMoves();

        // On vérifie simplement qu'aucun coup de pioche n'est proposé
        boolean hasStockMove = moves.stream()
                .anyMatch(m -> m.getType() == Move.MoveType.STOCK_TO_WASTE
                        || m.getType() == Move.MoveType.RECYCLE_WASTE);
        assertFalse(hasStockMove);
    }

    @Test
    void testTableauToFoundation_Generation() {
        // Setup : On place un As de Pique sur le tableau 0
        board.getTableaux().get(0).clear();
        Card ace = new Card(Suit.SPADES, 1, true);
        board.getTableaux().get(0).addCard(ace);

        // Action : On demande à l'IA ce qu'elle peut faire
        List<Move> moves = board.getValidMoves();

        // Vérification : L'IA doit voir qu'elle peut envoyer l'As en fondation
        boolean found = moves.stream()
                .anyMatch(m -> m.getType() == Move.MoveType.TABLEAU_TO_FOUNDATION
                        && m.getCard().equals(ace));

        assertTrue(found, "getValidMoves aurait dû proposer TABLEAU_TO_FOUNDATION pour l'As");
    }
}