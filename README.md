# 🃏 Solitaire IA - MCTS Solver

Une application de Solitaire (Klondike) développée en JavaFX, intégrant une **Intelligence Artificielle** performante basée sur l'algorithme **Monte Carlo Tree Search (MCTS)**.

## 🚀 Fonctionnalités

* **Jeu complet :** Interface graphique fluide pour jouer au Solitaire classique.
* **Intelligence Artificielle :**
    * Utilise l'algorithme MCTS pour explorer les coups possibles.
    * Système de poids heuristiques (Bonus pour les As, pénalités pour les retours en arrière, etc.).
* **Niveaux de difficulté :**
    * Génération de parties classées par difficulté (Facile, Moyen, Difficile, Extrême) grâce à une analyse pré-calculée des graines (Seeds).
* **Mode Analyse :** Possibilité d'entrer une "Seed" spécifique pour rejouer une même partie.
* **Mode Auto-Play :** Regardez l'IA résoudre la partie en temps réel.

## 🛠️ Prérequis

* **Java 21** (ou supérieur) doit être installé sur votre machine.

## 🎮 Comment jouer (Utilisateurs)

1.  Allez dans la section **[Releases](../../releases)** de ce dépôt.
2.  Téléchargez le fichier `.jar` de la dernière version.
3.  Lancez le jeu (double-clic ou via le terminal) :
    ```bash
    java -jar SolitaireIA_vf-1.0-SNAPSHOT.jar
    ```

## 💻 Installation (Développeurs)

Pour modifier le code ou compiler vous-même le projet :

1.  Cloner le dépôt :
    ```bash
    git clone https://github.com/lucasabeka/Solitaire-AI-MCTS.git
    ```
2.  Ouvrir avec IntelliJ IDEA.
3.  Compiler avec Maven :
    ```bash
    mvn clean package
    ```
