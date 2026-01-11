package ia;
import core.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class AITrainer {

    private static final long[] TRAIN_SEEDS = {
            13015695276460L, 13015694211139L, 13015695084161L, 13015694784064L, 13020911641616L,
            13022199080626L, 13021653139787L, 13024692672883L, 13025475355576L, 13027617921666L,
            13028571082560L, 13029730152250L, 13030738493409L, 13030699917879L, 13031494895889L,
            13032165442514L, 13033249495218L, 13034848222536L, 13035686816516L, 13038071247561L,
            13038367976888L, 13039764882443L, 13042773845601L, 13043945840721L, 13046133912277L,
            13047347086903L, 13050357302713L, 13046824664936L, 13048009819003L, 13051923869096L,
            13050555583792L, 13051892763943L, 13052670195540L, 13054072816500L, 13055118761982L,
            13054936993590L, 13056293924737L, 13059569079963L, 13060390654068L, 13062261637499L,
            13063253303367L, 13065701187387L, 13066562921549L, 13067530350800L, 13067520626758L,
            13070230818607L, 13069535514345L, 13070790698305L, 13068949983124L, 13070161539362L
    };

    public static void main(String[] args) {
        System.out.println("🧬 Démarrage de l'évolution de l'IA...");

        // Génération 0 : L'IA de base
        AIParams bestParams = new AIParams();
        double bestWinRate = 0.0;

        // On fait tourner 10 générations
        for (int gen = 1; gen <= 10; gen++) {
            System.out.println("\n=== GÉNÉRATION " + gen + " ===");

            // On crée 5 mutants basés sur le meilleur actuel
            List<AIParams> population = new ArrayList<>();
            population.add(bestParams); // On garde le champion
            for (int i = 0; i < 4; i++) population.add(bestParams.mutate());

            AIParams generationChampion = bestParams;
            double generationBestRate = -1.0;

            // On teste chaque mutant
            for (AIParams mutant : population) {
                double winRate = evaluate(mutant);
                System.out.printf("Mutant [%s] -> WinRate: %.1f%%\n", mutant, winRate);

                if (winRate > generationBestRate) {
                    generationBestRate = winRate;
                    generationChampion = mutant;
                }
            }

            // Mise à jour du champion global
            if (generationBestRate >= bestWinRate) {
                bestParams = generationChampion;
                bestWinRate = generationBestRate;
                System.out.println("👑 NOUVEAU CHAMPION ! Taux de victoire : " + bestWinRate + "%");
            } else {
                System.out.println("❌ Aucun progrès cette génération.");
            }
        }

        System.out.println("\n✅ ÉVOLUTION TERMINÉE.");
        System.out.println("Meilleurs paramètres trouvés :");
        System.out.println(bestParams);
    }

    // Fait jouer 50 parties à une IA et retourne le % de victoire
    // Une base fixe pour que tous les mutants jouent les MÊMES parties
    // On peut changer cette base à chaque nouvelle génération pour éviter le "sur-apprentissage" (overfitting)

    private static double evaluate(AIParams params) {
        int totalGames = TRAIN_SEEDS.length; // 100 parties identiques pour tout le monde
        AtomicInteger wins = new AtomicInteger(0);

        IntStream.range(0, totalGames).parallel().forEach(i -> {
            Board board = new Board();

            // CORRECTION CRUCIALE :
            // Au lieu de System.currentTimeMillis() (Aléatoire),
            // On utilise (SEED_BASE + i).
            // Le thread 0 jouera toujours la graine 12345.
            // Le thread 1 jouera toujours la graine 12346.
            // Etc.
            board.newGame(TRAIN_SEEDS[i]);

            MCTSSolver solver = new MCTSSolver(params);

            int moves = 0;
            while (!board.isGameWon() && moves < 200) {
                Move m = solver.findBestMove(board);
                if (m == null) break;
                board.applyMove(m);
                moves++;
            }

            if (board.isGameWon()) {
                wins.incrementAndGet();
            }
        });

        return (wins.get() * 100.0) / totalGames;
    }
}
