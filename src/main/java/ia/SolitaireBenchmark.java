package ia;

import core.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class SolitaireBenchmark {
    // Nombre de parties à jouer (commence par 50 ou 100)
    private static final int TOTAL_GAMES = 50;
    // Limite de coups pour éviter les parties infinies
    private static final int MAX_MOVES_PER_GAME = 400;

    public static void main(String[] args) {
        System.out.println("=== Démarrage du Benchmark Solitaire AI ===");
        System.out.println("Simulation de " + TOTAL_GAMES + " parties en cours...");

        long startTime = System.currentTimeMillis();
        AtomicInteger wins = new AtomicInteger(0);
        AtomicInteger totalMoves = new AtomicInteger(0);

        // Utilisation du parallélisme pour utiliser tous les cœurs de ton processeur
        IntStream.range(0, TOTAL_GAMES).parallel().forEach(i -> {
            if (playSingleGame(i)) {
                wins.incrementAndGet();
                // Petit point pour montrer que ça avance (optionnel)
                System.out.print(".");
            } else {
                System.out.print("x");
            }
        });

        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;

        System.out.println("\n\n=== RÉSULTATS ===");
        System.out.println("Parties jouées : " + TOTAL_GAMES);
        System.out.println("Victoires      : " + wins.get());
        System.out.println("Taux de succès : " + (wins.get() * 100.0 / TOTAL_GAMES) + "%");
        System.out.println("Temps total    : " + duration + " secondes");
        System.out.println("Vitesse        : " + (TOTAL_GAMES / duration) + " parties/seconde");
    }

    private static boolean playSingleGame(int seedOffset) {
        Board board = new Board();
        // On change la graine à chaque partie pour avoir des donnes différentes
        board.newGame(System.currentTimeMillis() + seedOffset);

        MCTSSolver solver = new MCTSSolver();
        int moves = 0;

        while (!board.isGameWon() && moves < MAX_MOVES_PER_GAME) {
            Move bestMove = solver.findBestMove(board);

            if (bestMove == null) {
                return false; // Bloqué
            }

            board.applyMove(bestMove);
            moves++;
        }

        return board.isGameWon();
    }
}