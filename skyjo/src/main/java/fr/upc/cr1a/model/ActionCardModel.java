package fr.upc.cr1a.model;

/**
 * @author Nassim
 * @extends la classe CardModel
 * Classe permettant de créer des cartes action (carte spéciale)
 */
public class ActionCardModel extends CardModel {

    public enum ActionType {
            TRADE_CARD_WITH_PLAYER,        // remplacer une carte avec un autre joueur                        DONE
            REPLACE_CARD_IN_HAND,          // deplacer deux cartes dans ta main                               DONE
            PLAY_TWO_TURN,                 // jouer deux fois
            DRAW_THREE_CARD,               // piocher 3 cartes -› n'en garde 1 seule ou 0                     DONE
            PLAY_LAST_ACTION_IN_DEFAUSSE,  // jouer la derniere carte action defaussée                        DONE
            DEFENSE,                       // carte defense qui annule une action                             on fait une carte defense ou pas ?
            ALL_PLAYERS_DROP_CARD,         // chaque joueur doit defausser une carte choisie par le joueur    WORK
            SPY_ONE_COLUMN                 // regarder une colonne de carte de sa main                        DONE
    }

    ActionType type;

    public ActionCardModel(boolean faceDown, String pathImage, ActionType type) {
        super(faceDown, pathImage);
        this.type = type;
    }

    public ActionCardModel(boolean faceDown, ActionType type) {
        super(faceDown, null);
        this.type = type;
    }

    public ActionType getType() {
        return this.type;
    }



}

