import core.*;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class TableauTest {

    @Test
    void testCanAddCard_Empty() {
        Tableau tableau = new Tableau();

        // Branche : if (isEmpty()) -> Roi obligatoire
        Card king = new Card(Suit.HEARTS, 13, true);
        assertTrue(tableau.canAddCard(king), "Devrait accepter un Roi sur vide");

        Card queen = new Card(Suit.HEARTS, 12, true);
        assertFalse(tableau.canAddCard(queen), "Devrait refuser une Dame sur vide");
    }

    @Test
    void testCanAddCard_NonEmpty_ValidMove() {
        Tableau tableau = new Tableau();
        // On place un Roi de Cœur (Rouge)
        tableau.addCard(new Card(Suit.HEARTS, 13, true));

        // On tente de poser une Dame de Pique (Noir) -> OK
        Card blackQueen = new Card(Suit.SPADES, 12, true);
        assertTrue(tableau.canAddCard(blackQueen));
    }

    @Test
    void testCanAddCard_NonEmpty_WrongColor() {
        Tableau tableau = new Tableau();
        tableau.addCard(new Card(Suit.HEARTS, 13, true)); // Rouge

        // On tente Dame de Carreau (Rouge) -> Erreur
        Card redQueen = new Card(Suit.DIAMONDS, 12, true);
        assertFalse(tableau.canAddCard(redQueen), "Devrait refuser Rouge sur Rouge");
    }

    @Test
    void testCanAddCard_NonEmpty_WrongRank() {
        Tableau tableau = new Tableau();
        tableau.addCard(new Card(Suit.HEARTS, 13, true)); // Roi

        // On tente Valet de Pique (Noir, mais rang 11 au lieu de 12) -> Erreur
        Card blackJack = new Card(Suit.SPADES, 11, true);
        assertFalse(tableau.canAddCard(blackJack), "Devrait refuser un saut de rang");

        // On tente un As (Rang 1) -> Erreur
        Card ace = new Card(Suit.SPADES, 1, true);
        assertFalse(tableau.canAddCard(ace));
    }

    @Test
    void testGetVisibleCards() {
        Tableau tableau = new Tableau();
        // Cas 1: Tableau vide
        assertTrue(tableau.getVisibleCards().isEmpty());

        // Cas 2: Mélange de cartes cachées et visibles
        tableau.addCard(new Card(Suit.CLUBS, 10, false)); // Cachée
        tableau.addCard(new Card(Suit.HEARTS, 9, false)); // Cachée
        tableau.addCard(new Card(Suit.SPADES, 8, true));  // Visible
        tableau.addCard(new Card(Suit.DIAMONDS, 7, true)); // Visible

        Stack<Card> visible = tableau.getVisibleCards();
        assertEquals(2, visible.size());
        assertEquals(8, visible.get(0).getRank()); // La 8 est en bas de la pile visible
        assertEquals(7, visible.get(1).getRank()); // La 7 est en haut
    }

    @Test
    void testGetTopCards() {
        Tableau tableau = new Tableau();
        tableau.addCard(new Card(Suit.CLUBS, 1, true));
        tableau.addCard(new Card(Suit.CLUBS, 2, true));
        tableau.addCard(new Card(Suit.CLUBS, 3, true));

        // On demande les 2 cartes du haut
        Stack<Card> top2 = tableau.getTopCards(2);

        assertEquals(2, top2.size());
        assertEquals(2, top2.get(0).getRank()); // Doit être le 2
        assertEquals(3, top2.get(1).getRank()); // Doit être le 3
    }
}