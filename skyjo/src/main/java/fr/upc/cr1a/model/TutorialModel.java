package fr.upc.cr1a.model;

import java.util.ArrayList;
import java.util.List;

public class TutorialModel {
    public enum TutorialMode { CLASSIC, ACTION }
    
    private List<String> classicPages;
    private List<String> actionPages;
    private TutorialMode currentMode = null;
    private int currentPage = 0;

    public TutorialModel() {
        //Skyjo Classique
        classicPages = new ArrayList<>();
        classicPages.add("SKYJO CLASSIQUE  : Mise en place et but du jeu\n\nChaque joueur possède 12 cartes face cachée,\norganisées en 3 lignes de 4 cartes.\n\nLes cartes ont des valeurs entre -2 et 12.\n\nLe but est d'obtenir le score le plus faible!");
        classicPages.add("SKYJO CLASSIQUE  : Début et Fin de la manche\nAu début de la partie:\nChaque joueur retourne 2 cartes.\nCelui qui a la somme des cartes révélées la plus élevée commence à jouer.\n\nFin de la manche:\nQuand un joueur a retourné ses 12 cartes:\n- Les autres joueurs jouent 1 tour final\n- On calcule les scores\nATTENTION celui qui finit en premier et qui n'a pas le score le plus faible\nvoit ses points doublés!\n\nOn continue jusqu'à ce qu'un joueur atteigne 100 points.\nLe premier à 100 points a perdu!^^");
        classicPages.add("SKYJO CLASSIQUE  : Votre tour\n\nAction possible: \n 1. Prendre de la pioche\n    -Vous pouvez l'échanger avec une de vos cartes dans votre main.\n   - Ou bien vous pouvez la défausser et ensuite révéler une carte de votre main.\n\n2. Prendre de la défausse\n    -Vous devez échanger la carte de la défausse avec une cartes de la main.\n\n 3. Règle spéciale : Si suite a une échange ou après avoir révélé une carte,\nVous possédez 3 cartes de même valeur dans une même colonne,\nVous pouvez les retirer et les mettre sur la défausse");

        //Skyjo Action
        actionPages = new ArrayList<>();
        actionPages.add("SKYJO ACTION \n\nC'est Skyjo Classique avec des cartes en plus!\nDe nouvelles possibilités à chaque tour:\nChaque tour: 4 choix possibles\n\n1. Tirer une carte de la pioche\n2. Tirer une carte de la défausse\n3. Tirer une carte action\n4. Utiliser une carte action déjà acquise\n\n Chacune de ces possibilités termine le tour du joueur");
        actionPages.add("SKYJO ACTION - Les Cartes Action\n\n1. TRADE: Échange une carte de ta main avec une carte d'un autre joueur\n2. REPLACE: Echange la position de 2 cartes dans ta main\n3. TWO TURNS: Faire 2 tours en plus\n4. REPLAY: Joue la dernière carte action dans la défausse\n5. DROP: Le joueur défausse une carte à tous les autres joueurs\n6. SPY: Regarde secrètement une colonne de la main pendant 5s\nATTENTION:On ne peut pas utiliser de cartes action après l'avoir piochée.\n\nRègle spéciale:\n - On peut maintenant éliminer une ligne de 4 cartes identiques\n - Les cartes étoiles inutilisé à la fin de la manche vaut 10 points.");
        actionPages.add("SKYJO ACTION - Cartes Spéciales\n\nCARTES ÉTOILE:\n - Elle a une valeur de 0 point\n - Peut agir comme une carte joker,\n   Remplace la valeur d'une carte pour faire disparaître une colonne\n\nFaire une colonne étoile = -10 points\nFaire une ligne étoile = -15 points");
    }

    public void setMode(TutorialMode mode) {
        this.currentMode = mode;
        this.currentPage = 0;
    }

    public String getCurrentPageContent() {
        if (currentMode == null) return "";
        List<String> pages = currentMode == TutorialMode.CLASSIC ? classicPages : actionPages;
        if (currentPage >= 0 && currentPage < pages.size()) {
            return pages.get(currentPage);
        }
        return "";
    }

    public void nextPage() {
        if (currentMode == null) return;
        List<String> pages = currentMode == TutorialMode.CLASSIC ? classicPages : actionPages;
        if (currentPage < pages.size() - 1) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    public boolean hasNextPage() {
        if (currentMode == null) return false;
        List<String> pages = currentMode == TutorialMode.CLASSIC ? classicPages : actionPages;
        return currentPage < pages.size() - 1;
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    public int getCurrentPageNumber() {
        return currentPage + 1;
    }

    public int getTotalPages() {
        if (currentMode == null) return 0;
        List<String> pages = currentMode == TutorialMode.CLASSIC ? classicPages : actionPages;
        return pages.size();
    }

    public void reset() {
        currentPage = 0;
    }

    public TutorialMode getCurrentMode() {
        return currentMode;
    }
}

