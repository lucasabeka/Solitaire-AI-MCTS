package ia;

import core.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

public class SeedClassifier {

    // TA LISTE DE SEEDS SPÉCIFIQUE
    private static final long[] SEEDS_TO_TEST = {
            6433681377172L, 6433681302281L, 6433680627298L, 6433680485741L, 6433681068019L,
            6433681468645L, 6433681052319L, 6433681169080L, 6498089187400L, 6495210037535L,
            6536416482859L, 6535423034764L, 6523640279777L, 6569312315245L, 6546270623671L,
            6580573093563L, 6580551521711L, 6578819255279L, 6568582765772L, 6579450494868L,
            6596286972930L, 6601261982090L, 6623789108594L, 6625910443499L, 6617026601478L,
            6649168551511L, 6630937086344L, 6660522854433L, 6677714810269L, 6706005866093L
    };

    // Stockage thread-safe pour le tri
    private static final Map<String, List<Long>> classifiedSeeds = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("🏭 Démarrage de la classification ciblée...");
        System.out.println("Analyse de " + SEEDS_TO_TEST.length + " graines spécifiques en parallèle.");

        // Initialisation des listes
        classifiedSeeds.put("EASY", Collections.synchronizedList(new ArrayList<>()));
        classifiedSeeds.put("MEDIUM", Collections.synchronizedList(new ArrayList<>()));
        classifiedSeeds.put("HARD", Collections.synchronizedList(new ArrayList<>()));
        classifiedSeeds.put("EXTREME", Collections.synchronizedList(new ArrayList<>()));

        AtomicInteger processed = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        // Analyse Parallèle sur TA liste
        LongStream.of(SEEDS_TO_TEST).parallel().forEach(seed -> {
            String category = classify(seed);

            if (category != null) {
                classifiedSeeds.get(category).add(seed);
            } else {
                // Optionnel : Afficher celles qui sont impossibles/trop dures
                // System.out.println("Seed " + seed + " non résolue (trop dure ?)");
            }

            int count = processed.incrementAndGet();
            if (count % 5 == 0) System.out.print(".");
        });

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("\n\n✅ Analyse terminée en " + duration + "s.");

        // --- AFFICHAGE DU CODE À COPIER ---
        printJavaCodeBlock("EASY_SEEDS", classifiedSeeds.get("EASY"));
        printJavaCodeBlock("MEDIUM_SEEDS", classifiedSeeds.get("MEDIUM"));
        printJavaCodeBlock("HARD_SEEDS", classifiedSeeds.get("HARD"));
        printJavaCodeBlock("EXTREME_SEEDS", classifiedSeeds.get("EXTREME"));
    }

    // Détermine la catégorie d'une graine
    private static String classify(long seed) {
        // 1. Test TRÈS FACILE / FACILE (IA Bête - 50 sims)
        if (canSolve(seed, 50)) return "EASY";

        // 2. Test MOYEN (IA Standard - 200 sims)
        if (canSolve(seed, 200)) return "MEDIUM";

        // 3. Test DIFFICILE (IA Forte - 1000 sims)
        if (canSolve(seed, 1000)) return "HARD";

        // 4. Test EXTRÊME (IAmax - 5000 sims)
        if (canSolve(seed, 5000)) return "EXTREME";

        return null; // Trop dur ou impossible
    }

    // Helper pour tester une résolution
    private static boolean canSolve(long seed, int sims) {
        // On tente 2 fois pour la stabilité
        for(int i=0; i<2; i++) {
            Board board = new Board();
            board.newGame(seed);

            // On suppose que tu as le constructeur MCTSSolver(params, simulationCount)
            // Si tu ne l'as pas, utilise le constructeur standard, mais le classement sera moins précis
            MCTSSolver solver = new MCTSSolver(new AIParams(), sims);

            int moves = 0;
            while (!board.isGameWon() && moves < 200) {
                Move m = solver.findBestMove(board);
                if (m == null) break;
                board.applyMove(m);
                moves++;
            }
            if (board.isGameWon()) return true;
        }
        return false;
    }

    private static void printJavaCodeBlock(String varName, List<Long> seeds) {
        System.out.println("\n// Copie ce bloc dans DifficultyDatabase.java (" + seeds.size() + " seeds) :");
        System.out.println("private static final long[] " + varName + " = {");

        StringBuilder sb = new StringBuilder("    ");
        for (int i = 0; i < seeds.size(); i++) {
            sb.append(seeds.get(i)).append("L");
            if (i < seeds.size() - 1) sb.append(", ");

            if ((i + 1) % 5 == 0 && i < seeds.size() - 1) {
                sb.append("\n    ");
            }
        }
        System.out.println(sb.toString());
        System.out.println("};");
    }
}