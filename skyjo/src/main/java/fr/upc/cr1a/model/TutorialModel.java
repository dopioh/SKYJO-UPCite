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
        classicPages.add("SKYJO CLASSIQUE  Objectif du jeu\n\nChaque joueur possède 12 cartes face cachée,\norganisées en 3 colonnes de 4 cartes.\n\nLes cartes ont des valeurs entre -2 et 12.\n\nLe BUT: obtenir la somme la plus faible!");
        classicPages.add("SKYJO CLASSIQUE  Fin de la manche\n\nQuand un joueur a retourné ses 12 cartes:\n- Les autres joueurs jouent 1 tour final\n- On calcule les scores\n\nCelui qui finit en premier sans\nle meilleur score voit ses points DOUBLÉS!\n\nLe premier à 100 points a perdu!");
        classicPages.add("SKYJO CLASSIQUE  Début de manche\n\nAu premier tour:\n- Chaque joueur retourne 2 cartes\n\nCelui qui a la somme des 2 cartes révélés la plus élevée commence a jouer");
        classicPages.add("SKYJO CLASSIQUE  Votre tour\n\nAction possible: \n 1.Piocher\n    -Vous pouvez l'échanger avec une de vos cartes dans votre main.\n   - Ou bien vous pouvez la défausser et ensuite révéler une carte de votre main.");

        //Skyjo Action
        actionPages = new ArrayList<>();
        actionPages.add("SKYJO ACTION   Qu'est-ce que le Mode Action?\n\nC'est Skyjo Classique avec 6 CARTES SPÉCIALES!\n\nCes cartes action changent les règles\net rendent le jeu plus CHAOTIQUE!\n\nLes cartes action ont une pioche\net une défausse séparées.");
        actionPages.add("SKYJO ACTION   Cartes Action - Partie 1\n\n🔄 ÉCHANGE:\nÉchangez une de vos cartes avec un adversaire.\n\n↔️ REPLACE:\nÉchangez la position de 2 de vos cartes.");
        actionPages.add("SKYJO ACTION   Cartes Action - Partie 2\n\n2️⃣ JOUE 2 FOIS:\nGagnez un tour supplémentaire\nà la fin de ce tour!\n\n🔁 REJOUE:\nRejouez la dernière action jouée\nde la défausse action.");
        actionPages.add("SKYJO ACTION   Cartes Action - Partie 3\n\n👥 CHAQUE JOUEUR DÉFAUSSE:\nPour chaque adversaire:\n→ Choisissez UNE de leurs cartes\n→ Remplacez-la par une de la pioche\n\n👀 REGARDE:\nRévèle pendant 3 secondes\ntoutes les cartes d'une colonne!");
        actionPages.add("SKYJO ACTION   Comment jouer une carte Action?\n\nVotre tour, vous pouvez:\n\n1. Piocher UNE CARTE CLASSIQUE\n   → Garder ou défausser (normal)\n\n2. Piocher UNE CARTE ACTION\n   → Votre tour s'ARRÊTE immédiatement!\n\n3. JOUER UNE CARTE ACTION\n   → Appliquez l'effet\n   → Votre tour s'ARRÊTE!\n   → Impossible de piocher après");
        actionPages.add("SKYJO ACTION   Stratégie en Mode Action\n\n⚠️ Les cartes action créent:\n→ Plus de rebondissements\n→ Plus de stratégie\n→ Plus de chaos!\n\n💡 Conseil:\nUtilisez les actions pour bloquer\nos adversaires ou améliorer votre main!\n\nLa partie est plus imprévisible!");
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

