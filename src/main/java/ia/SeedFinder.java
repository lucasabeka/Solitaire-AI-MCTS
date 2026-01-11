package ia;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import core.*;

public class SeedFinder {
    // Combien de graines gagnantes voulons-nous trouver ?
    private static final int TARGET_WINS = 30;

    public static void main(String[] args) {
        System.out.println("⛏️ Recherche de " + TARGET_WINS + " parties gagnables...");

        List<Long> winningSeeds = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger attempts = new AtomicInteger(0);

        // On cherche en parallèle jusqu'à avoir trouvé assez de graines
        // Note : On utilise une stream infinie qu'on arrêtera manuellement
        IntStream.generate(attempts::getAndIncrement)
                .parallel()
                .filter(i -> winningSeeds.size() < TARGET_WINS)
                .forEach(i -> {
                    long seed = System.nanoTime() + (i * 9999L); // Génération de graine unique

                    if (isWinnable(seed)) {
                        winningSeeds.add(seed);
                        System.out.print("💎"); // Petit indicateur visuel

                        // Si on a fini, on affiche tout le bloc
                        if (winningSeeds.size() == TARGET_WINS) {
                            printJavaArray(winningSeeds);
                            System.exit(0); // On coupe tout brutalement quand c'est fini
                        }
                    }
                });
    }

    // Joue une partie rapide avec l'IA actuelle
    private static boolean isWinnable(long seed) {
        Board board = new Board();
        board.newGame(seed);

        // On utilise le mode "Training" (rapide) ou normal selon la puissance de ton PC
        // Ici on met 'false' (500 sims) pour être sûr que si on dit "gagnable", elle l'est vraiment.
        MCTSSolver solver = new MCTSSolver(new AIParams(), false);

        int moves = 0;
        while (!board.isGameWon() && moves < 250) {
            Move m = solver.findBestMove(board);
            if (m == null) return false;
            board.applyMove(m);
            moves++;
        }
        return board.isGameWon();
    }

    // Affiche les graines sous forme de tableau Java prêt à copier/coller
    private static void printJavaArray(List<Long> seeds) {
        System.out.println("\n\n✅ VOICI TES GRAINES GAGNABLES (Copie ce bloc) :");
        System.out.println("private static final long[] TRAIN_SEEDS = {");
        for (int i = 0; i < seeds.size(); i++) {
            System.out.print(seeds.get(i) + "L");
            if (i < seeds.size() - 1) System.out.print(", ");
            if ((i + 1) % 5 == 0) System.out.println(); // Saut de ligne tous les 5
        }
        System.out.println("};");
    }
}