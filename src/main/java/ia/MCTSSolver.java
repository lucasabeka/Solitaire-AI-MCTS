package ia;
import core.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class MCTSSolver {
    private static final int SIMULATIONS = 150; // Un peu moins de simulations pour garder la fluidité
    private static final int MAX_DEPTH = 100;

    private final AIParams params;

    private int simulationCount;

    // Constructeur par défaut (utilise les params de base)
    public MCTSSolver() {
        this.params = new AIParams();
        this.simulationCount = 1000;
    }

    public MCTSSolver(AIParams params, int simulationCount) {
        this.params = params;
        this.simulationCount = simulationCount;
    }

    // Constructeur avec ADN spécifique (pour l'apprentissage)
    public MCTSSolver(AIParams params) {
        this(params, false);
    }

    // Nouveau constructeur complet
    public MCTSSolver(AIParams params, boolean trainingMode) {
        this.params = params;
        // Si mode entraînement : 50 simulations (très rapide)
        // Sinon (jeu réel) : 500 simulations (précis)
        this.simulationCount = trainingMode ? 50 : 1000;
    }

    public Move findBestMove(Board realBoard) {
        List<Move> availableMoves = realBoard.getValidMoves();
        if (availableMoves.isEmpty()) return null;

        // --- PHASE 1 : L'INSTINCT (Règles Prioritaires) ---
        // Si on peut faire un coup génial, on le fait tout de suite sans lancer de simulations.

        // 1. Monter une carte en fondation (C'est toujours bon)
        for (Move m : availableMoves) {
            if (m.getType() == Move.MoveType.TABLEAU_TO_FOUNDATION ||
                    m.getType() == Move.MoveType.WASTE_TO_FOUNDATION) {
                return m;
            }
        }

        // 2. Retourner une carte cachée (Le but du jeu !)
        for (Move m : availableMoves) {
            if (m.getType() == Move.MoveType.TABLEAU_TO_TABLEAU) {
                Tableau src = realBoard.getTableaux().get(m.getSourceIndex());
                // Si on déplace tout ce qui reste visible, et qu'il y a des cartes cachées dessous...
                if (m.getSequenceLength() == src.getVisibleCards().size() && src.size() > src.getVisibleCards().size()) {
                    return m; // ...alors ce coup va retourner la carte du dessous. ON FONCE !
                }
            }
        }

        // 3. Jouer un As ou un 2 (Urgence absolue)
        for (Move m : availableMoves) {
            if (m.getCard() != null && m.getCard().getRank() <= 2) {
                // Sauf si c'est pour le redescendre d'une fondation
                if (m.getType() != Move.MoveType.FOUNDATION_TO_TABLEAU) return m;
            }
        }

        // --- PHASE 2 : LA RÉFLEXION (MCTS) ---
        // Si aucun coup évident, on réfléchit pour départager les choix complexes.

        // Si un seul coup reste (souvent la pioche), on le joue.
        if (availableMoves.size() == 1) return availableMoves.get(0);

        Map<Move, Double> moveScores = new ConcurrentHashMap<>();
        Map<Move, Integer> moveCounts = new ConcurrentHashMap<>();

        IntStream.range(0, simulationCount).parallel().forEach(i -> {
            // Chaque thread doit avoir sa propre copie du plateau !
            Board simulationBoard = realBoard.clone();

            Move firstMove = selectInitialMove(availableMoves);

            simulationBoard.applyMove(firstMove);
            double score = simulateRandomGame(simulationBoard);

            // merge est "thread-safe" sur une ConcurrentHashMap
            moveScores.merge(firstMove, score, Double::sum);
            moveCounts.merge(firstMove, 1, Integer::sum);
        });

        // Sélection du meilleur coup selon la moyenne des scores
        return availableMoves.stream()
                .max(Comparator.comparingDouble(m -> getAverageScore(m, moveScores, moveCounts)))
                .orElse(availableMoves.get(0));
    }

    public Move selectInitialMove(List<Move> moves) {
        // Petite astuce : on préfère tester les mouvements de tableau plutôt que la pioche
        List<Move> tableauMoves = moves.stream()
                .filter(m -> m.getType() == Move.MoveType.TABLEAU_TO_TABLEAU)
                .toList();

        if (!tableauMoves.isEmpty() && ThreadLocalRandom.current().nextDouble() < 0.7) {
            return tableauMoves.get(ThreadLocalRandom.current().nextInt(tableauMoves.size()));
        }
        return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
    }

    public double getAverageScore(Move m, Map<Move, Double> scores, Map<Move, Integer> counts) {
        double avg = scores.getOrDefault(m, -1000.0) / counts.getOrDefault(m, 1);

        // PETIT HACK FINAL : On pénalise artificiellement la pioche dans le score final
        if (m.getType() == Move.MoveType.STOCK_TO_WASTE) {
            avg += params.stockPenalty;
        }
        return avg;
    }

    public double simulateRandomGame(Board board) {
        int movesCount = 0;
        // Pour éviter les boucles, on limite la profondeur
        while (!board.isGameWon() && movesCount < MAX_DEPTH) {
            List<Move> moves = board.getValidMoves();
            if (moves.isEmpty()) break;

            // Dans la simulation, on joue intelligemment (90% heuristique, 10% hasard)
            Move nextMove = selectSimulationMove(board, moves);
            board.applyMove(nextMove);
            movesCount++;
        }
        return calculateScore(board);
    }

    public Move selectSimulationMove(Board board, List<Move> moves) {
        // Hasard pour l'exploration
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
        }

        // Sinon, meilleur coup local
        return moves.stream()
                .max(Comparator.comparingDouble(m -> evaluateMoveImmediateValue(board, m)))
                .orElse(moves.get(0));
    }

    // L'évaluation locale (pour la simulation)
    public int evaluateMoveImmediateValue(Board board, Move move) {
        double score = 0;
        switch (move.getType()) {
            case TABLEAU_TO_FOUNDATION -> score += params.foundationBonus; // Priorité MAX
            case WASTE_TO_FOUNDATION -> score += params.foundationBonus;
            case TABLEAU_TO_TABLEAU -> {
                score += params.tableauMoveBonus;
                // Si on révèle une carte cachée
                Tableau src = board.getTableaux().get(move.getSourceIndex());
                int remaining = src.size() - move.getSequenceLength();
                if (remaining > 0 && !src.getCards().get(remaining - 1).isFaceUp()) {
                    score += params.revealBonus; // BOOM ! C'est ce qu'on veut
                }
            }
            case WASTE_TO_TABLEAU -> score += params.wasteToTableauBonus;
            case STOCK_TO_WASTE -> score += params.stockPenaltyImmediate; // On n'aime pas piocher
            case RECYCLE_WASTE -> score += params.recyclePenalty;
            case FOUNDATION_TO_TABLEAU -> score += params.foundationToTableauPenalty;
        }

        // Bonus pour les Rois sur vide
        if (move.getCard() != null && move.getCard().getRank() == 13) {
            if (move.getType() == Move.MoveType.TABLEAU_TO_TABLEAU && move.getSourceIndex() != -1) {
                Tableau src = board.getTableaux().get(move.getSourceIndex());
                // Si le Roi n'est pas déjà à la base d'une pile
                if (src.getCards().indexOf(move.getCard()) > 0) score += params.kingBonus;
            }
        }

        return (int) score;
    }

    public int calculateScore(Board board) {
        if (board.isGameWon()) return 100000;
        int score = 0;
        // On récompense les fondations
        for (Foundation f : board.getFoundations()) score += f.size() * 500;
        // On récompense énormément les cartes retournées sur le tableau
        for (Tableau t : board.getTableaux()) score += t.getVisibleCards().size() * 100;
        return score;
    }
}