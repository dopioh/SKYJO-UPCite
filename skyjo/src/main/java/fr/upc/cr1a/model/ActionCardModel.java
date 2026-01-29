package fr.upc.cr1a.model;

/**
 * @author Nassim
 * @extends la classe CardModel
 * Classe permettant de créer des cartes action (carte spéciale)
 */
public class ActionCardModel extends CardModel {
    public ActionCardModel(boolean faceDown, String pathImage) {
        super(faceDown, pathImage);
    }

    // constructeur NumberCardModel sans image
    public ActionCardModel(boolean faceDown) {
        super(faceDown, null);
    }

}

