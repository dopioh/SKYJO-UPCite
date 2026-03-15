package fr.upc.cr1a.model;

import java.util.Collections;

import fr.upc.cr1a.model.ActionCardModel.ActionType;

/**
 * @author Nassim
 * @extends la classe CardStackModel
 * Classe permettant de créer des pioches de carte action (pile de 24 cartes)
 */
public class PiocheActionModel extends CardStackModel {

    public PiocheActionModel() {
        for (ActionType type : ActionType.values()) {
            if (type != ActionType.DEFENSE) {
                for (int k = 0; k < 3; k++) {
                    this.getStack().add(new ActionCardModel(true, ActionType.ALL_PLAYERS_DROP_CARD));
                }
            }
        }
        Collections.shuffle(this.getStack());
    }

    /**
     * Quand la pioche est vide et la reconstruit avec les cartes de la defausse
     * @param defausse
     */
    public void DefausseInPioche(DefausseActionModel defausse) {
        if (this.stackSize() == 0) {
            while (defausse.stackSize() > 1) {
                CardModel carte = defausse.getCardAt(0);
                this.addCardAtTop(carte);
                defausse.getStack().remove(0);
            }
        }
        Collections.shuffle(this.getStack());
    }

}
