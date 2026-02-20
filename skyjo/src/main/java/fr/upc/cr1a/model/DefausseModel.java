package fr.upc.cr1a.model;

/**
 * @author Nassim,hugo
 * @extends la classe CardStackModel
 * Classe permettant de créer des defausses de jeu (pile de 150 cartes vides)
 */
public class DefausseModel extends CardStackModel {

    public DefausseModel() {
        super();
    }
    
    @Override
    public void addCardAtTop(CardModel carte) {
        if (carte != null && carte.isFaceDown()) {
            carte.returnCard(); 
        }
        super.addCardAtTop(carte);
    }
}
