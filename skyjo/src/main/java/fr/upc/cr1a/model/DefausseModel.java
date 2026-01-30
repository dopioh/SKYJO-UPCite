package fr.upc.cr1a.model;

/**
 * @author Nassim
 * @extends la classe CardStackModel
 * Classe permettant de créer des defausses de jeu (pile de 150 cartes vides)
 */
public class DefausseModel extends CardStackModel {

    public DefausseModel() {
        for (int i = 0; i < 151; i++) {
                this.Stack.add(new NumberCardModel(false, -99, "vide"));
        }
    }
    
}
