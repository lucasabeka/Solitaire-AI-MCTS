package ui;

import core.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Stack;

public class SolitaireView extends BorderPane {
    private final Board board;
    private final HBox topZone;
    private final HBox bottomZone;

    public SolitaireView(Board board) {
        this.board = board;
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #2E8B57;"); // Vert tapis de jeu

        // Zone du haut : Stock, Waste, Fondations
        topZone = new HBox(15);
        topZone.setAlignment(Pos.CENTER_LEFT);

        // Zone du bas : Les 7 Tableaux
        bottomZone = new HBox(20);
        bottomZone.setAlignment(Pos.TOP_CENTER);
        bottomZone.setPadding(new Insets(20, 0, 0, 0));

        this.setTop(topZone);
        this.setCenter(bottomZone);

        refresh();
    }

    public void refresh() {
        topZone.getChildren().clear();
        bottomZone.getChildren().clear();

        // 1. DESSINER LE HAUT
        // Pioche (Stock)
        topZone.getChildren().add(createCardStackView(board.getStock(), true));

        // Défausse (Waste)
        topZone.getChildren().add(createCardStackView(board.getWaste(), false));

        // Espacement flexible
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topZone.getChildren().add(spacer);

        // Fondations
        for (Foundation f : board.getFoundations()) {
            topZone.getChildren().add(createCardStackView(f.getCards(), false));
        }

        // 2. DESSINER LE BAS (Tableaux)
        for (Tableau t : board.getTableaux()) {
            bottomZone.getChildren().add(createTableauView(t));
        }
    }

    // Crée une vue pour une pile simple (Stock, Waste, Foundation)
    private StackPane createCardStackView(Stack<Card> pile, boolean forceFaceDown) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(70, 100);

        if (pile.isEmpty()) {
            // Emplacement vide (contour semi-transparent)
            Rectangle placeholder = new Rectangle(70, 100);
            placeholder.setFill(Color.TRANSPARENT);
            placeholder.setStroke(Color.LIGHTGRAY);
            placeholder.setStrokeWidth(2);
            placeholder.setArcWidth(10);
            placeholder.setArcHeight(10);
            stackPane.getChildren().add(placeholder);
        } else {
            // Dessiner juste la carte du dessus
            Card top = pile.peek();
            if (forceFaceDown) {
                stackPane.getChildren().add(drawCard(null)); // Dos de carte
            } else {
                stackPane.getChildren().add(drawCard(top));
            }
        }
        return stackPane;
    }

    // Crée une vue en cascade pour un tableau
    private Pane createTableauView(Tableau tableau) {
        Pane column = new Pane();
        column.setPrefWidth(70);
        column.setPrefHeight(400); // Assez haut pour la cascade

        if (tableau.isEmpty()) {
            Rectangle placeholder = new Rectangle(70, 100);
            placeholder.setFill(Color.TRANSPARENT);
            placeholder.setStroke(Color.DARKGREEN);
            column.getChildren().add(placeholder);
        } else {
            List<Card> cards = tableau.getCards();
            for (int i = 0; i < cards.size(); i++) {
                StackPane cardView = drawCard(cards.get(i));
                cardView.setLayoutY(i * 25); // Décalage vertical (cascade)
                column.getChildren().add(cardView);
            }
        }
        return column;
    }

    // DESSINE UNE CARTE (RECTANGLE + TEXTE)
    private StackPane drawCard(Card card) {
        StackPane cardGroup = new StackPane();

        // Fond de la carte
        Rectangle bg = new Rectangle(70, 100);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setStroke(Color.BLACK);

        if (card == null || !card.isFaceUp()) {
            // Carte face cachée (Dos bleu)
            bg.setFill(Color.CORNFLOWERBLUE);
            bg.setStroke(Color.WHITE);
        } else {
            // Carte face visible (Blanc)
            bg.setFill(Color.WHITE);

            // Texte (ex: "10♥")
            String rankStr = switch(card.getRank()) {
                case 1 -> "A";
                case 11 -> "J";
                case 12 -> "Q";
                case 13 -> "K";
                default -> String.valueOf(card.getRank());
            };

            String suitStr = switch(card.getSuit()) {
                case HEARTS -> "♥";
                case DIAMONDS -> "♦";
                case CLUBS -> "♣";
                case SPADES -> "♠";
            };

            Text text = new Text(rankStr + "\n" + suitStr);
            text.setFont(Font.font("Arial", 18));
            text.setFill(card.isRed() ? Color.RED : Color.BLACK);
            cardGroup.getChildren().add(text);
        }

        cardGroup.getChildren().add(0, bg); // Ajouter le fond en premier
        return cardGroup;
    }
}