package ui;

import core.*;
import ia.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField; // <--- Nouvel import
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceBox;


public class SolitaireApp extends Application {

    private Board board;
    private SolitaireView gameView;
    private MCTSSolver solver;
    private boolean isAiPlaying = false;

    private ChoiceBox<String> difficultySelector;
    private Label statusLabel;
    private TextField seedField; // <--- Nouveau champ

    private long startTime;
    private int moveCount;
    private boolean gameStarted; // Pour ne lancer le chrono qu'au premier mouvement

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Initialisation
        board = new Board();
        solver = new MCTSSolver();
        gameView = new SolitaireView(board);
        statusLabel = new Label("Prêt à jouer !");
        // Par défaut, on lance une partie aléatoire
        long initialSeed = System.currentTimeMillis();
        board.newGame(initialSeed);

        gameView = new SolitaireView(board);
        statusLabel = new Label("Seed: " + initialSeed);

        difficultySelector = new ChoiceBox<>();
        difficultySelector.getItems().addAll(
                "Aléatoire",
                "Facile",
                "Moyen",
                "Difficile",
                "Extrême"
        );
        difficultySelector.setValue("Aléatoire"); // Valeur par défaut
        difficultySelector.setPrefWidth(100);

        // 2. Contrôles (Boutons)
        Button btnStep = new Button("Jouer 1 Coup");
        btnStep.setOnAction(e -> playOneMove());

        Button btnAuto = new Button("IA Auto-Play");
        btnAuto.setOnAction(e -> toggleAutoPlay());

        Button btnReset = new Button("Nouvelle Partie");
        btnReset.setOnAction(e -> startNewGameWithDifficulty());


        // 2. Contrôles de Seed (Nouveau !)
        seedField = new TextField();
        seedField.setPromptText("Entrer Seed ici");
        seedField.setPrefWidth(120);

        Button btnLoadSeed = new Button("Charger Seed");
        btnLoadSeed.setOnAction(e -> loadSpecificSeed());

        HBox gameControls = new HBox(10,difficultySelector, btnReset, btnStep, btnAuto);
        gameControls.setAlignment(Pos.CENTER);

        HBox seedControls = new HBox(10, seedField, btnLoadSeed);
        seedControls.setAlignment(Pos.CENTER);

        VBox allControls = new VBox(10, gameControls, seedControls, statusLabel);
        allControls.setPadding(new javafx.geometry.Insets(10));
        allControls.setAlignment(Pos.CENTER);

        // 3. Layout Principal
        VBox root = new VBox(gameView, allControls);
        Scene scene = new Scene(root, 900, 700); // Un peu plus large pour les stats

        primaryStage.setTitle("Solitaire AI - MCTS Solver");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- GESTION DES PARTIES ---

    private void initStats() {
        moveCount = 0;
        gameStarted = false;
        // On ne lance pas le chrono tout de suite, on attend le premier coup
        // pour ne pas compter le temps de réflexion de l'utilisateur avant de cliquer.
    }

    private void startTimer() {
        if (!gameStarted) {
            startTime = System.currentTimeMillis();
            gameStarted = true;
        }
    }

    // Lance une partie avec une graine aléatoire
    private void startNewGameWithDifficulty() {
        stopAI();

        // 1. Récupérer le niveau choisi
        String selectedDifficulty = difficultySelector.getValue();

        // 2. Demander une graine correspondante à la database
        long seed = DifficultyDatabase.getSeedForDifficulty(selectedDifficulty);

        // 3. Lancer la partie
        board.newGame(seed);
        initStats();
        gameView.refresh();

        // 4. Feedback utilisateur
        statusLabel.setText("Mode: " + selectedDifficulty + " (Seed: " + seed + ")");
        statusLabel.setStyle("-fx-text-fill: black;");
        seedField.clear();
    }

    private void playOneMove() {
        if (board.isGameWon()) return;

        startTimer();

        // --- AJOUT DEBUG ---
        System.out.println("--- Analyse IA ---");
        System.out.println("Coups trouvés : " + board.getValidMoves().size());
        for (Move m : board.getValidMoves()) {
            System.out.println(" - " + m); // Affiche la liste. Si l'As n'est pas là, c'est Board.java le coupable !
        }
        // -------------------

        // Calculer le coup dans un Thread séparé pour ne pas figer l'interface
        new Thread(() -> {
            Move bestMove = solver.findBestMove(board);

            // Revenir sur le thread JavaFX pour mettre à jour l'interface
            Platform.runLater(() -> {
                if (bestMove != null) {
                    board.applyMove(bestMove);
                    moveCount++; // On compte le coup
                    gameView.refresh();

                    if (board.isGameWon()) {
                        showVictoryStats();
                        isAiPlaying = false;
                    } else {
                        // Affiche juste le coup en cours si pas fini
                        // statusLabel.setText("Coup " + moveCount + ": " + bestMove);
                    }
                } else {
                    statusLabel.setText("Bloqué après " + moveCount + " coups.");
                    isAiPlaying = false;
                }
            });
        }).start();
    }

    private void toggleAutoPlay() {
        if (isAiPlaying) {
            isAiPlaying = false;
            return;
        }

        if (board.isGameWon()) return;

        isAiPlaying = true;
        statusLabel.setText("L'IA réfléchit...");
        startTimer();

        // Lancer une boucle dans un thread séparé
        new Thread(() -> {
            while (isAiPlaying && !board.isGameWon()) {
                Move bestMove = solver.findBestMove(board);

                if (bestMove == null) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Bloqué après " + moveCount + " coups.");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    });
                    isAiPlaying = false;
                    break;
                }

                // Mise à jour visuelle
                Platform.runLater(() -> {
                    board.applyMove(bestMove);
                    moveCount++;
                    gameView.refresh();

                    if (board.isGameWon()) {
                        showVictoryStats();
                        isAiPlaying = false; // Stop la boucle
                    }
                });

                // Petite pause pour qu'on ait le temps de voir jouer l'IA
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    // Lance une partie avec la graine du champ de texte
    private void loadSpecificSeed() {
        stopAI();
        try {
            String text = seedField.getText();
            if (text.isEmpty()) return;

            // On gère le "L" à la fin si tu fais un copier-coller depuis SeedFinder
            text = text.replace("L", "").trim();

            long seed = Long.parseLong(text);
            board.newGame(seed);
            initStats();
            gameView.refresh();
            statusLabel.setText("Seed Chargée: " + seed);
        } catch (NumberFormatException e) {
            statusLabel.setText("Erreur: Seed invalide (Chiffres seulement)");
        }
    }

    private void showVictoryStats() {
        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;

        // Évite la division par zéro si c'est instantané
        if (durationSeconds < 0.1) durationSeconds = 0.1;

        double movesPerSecond = moveCount / durationSeconds;

        String stats = String.format(
                "VICTOIRE ! 🎉\nCoups : %d | Temps : %.2fs | Vitesse : %.2f coups/sec",
                moveCount, durationSeconds, movesPerSecond
        );

        statusLabel.setText(stats);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
    }

    private void stopAI() {
        isAiPlaying = false;
    }
}