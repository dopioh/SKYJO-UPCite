package fr.upc.cr1a.model;

import java.util.Collections;
import java.util.Random;

/**
 * @author Nassim
 * @extends la classe CardStackModel
 * Classe permettant de créer des pioches de jeu (pile de 150 cartes)
 */
public class PiocheModel extends CardStackModel {

    /**
     * Random utilise pour les melanges. Permet de seeder pour synchroniser host/clients en multi.
     */
    private final Random rng;

    public PiocheModel(boolean action) {
        this(action, new Random().nextLong());
    }

    /**
     * Constructeur seed : permet d'obtenir le meme ordre de cartes sur deux machines differentes
     * (host + clients en multijoueur) Le seed est diffusé via START_GAME
     */
    public PiocheModel(boolean action, long seed) {
        this.rng = new Random(seed);
        initStack(action);
    }

    private void initStack(boolean action) {
        if (!action) {
            int[] counts = {
                5,  // -2
                10, // -1
                15, // 0
                10, // 1
                10, // 2
                10, // 3
                10, // 4
                10, // 5
                10, // 6
                10, // 7
                10, // 8
                10, // 9
                10, // 10
                10, // 11
                10  // 12
            };

            for (int i = 0; i < counts.length; i++) {
                int value = -2 + i;

                String color;
                if (value < 0) color = "violet";
                else if (value == 0) color = "bleu";
                else if (value <= 4) color = "vert";
                else if (value <= 8) color = "jaune";
                else color = "rouge";

                for (int k = 0; k < counts[i]; k++) {
                    this.getStack().add(new NumberCardModel(true, value, color, false));
                }
            }
            Collections.shuffle(this.getStack(), rng);
        }
        else {
            int[] counts = {
                3,  // -2
                7, // -1
                11, // 0
                7, // 1
                7, // 2
                7, // 3
                7, // 4
                7, // 5
                7, // 6
                7, // 7
                7, // 8
                7, // 9
                7, // 10
                7, // 11
                7  // 12
            };

            for (int i = 0; i < counts.length; i++) {
                int value = -2 + i;

                String color;
                if (value < 0) color = "violet";
                else if (value == 0) color = "bleu";
                else if (value <= 4) color = "vert";
                else if (value <= 8) color = "jaune";
                else color = "rouge";

                for (int k = 0; k < counts[i]; k++) {
                    this.getStack().add(new NumberCardModel(true, value, color, false));
                }
            }
            int starCount = 15;
            for (int j = 0; j < starCount; j++) {
                this.getStack().add(new NumberCardModel(true, 0, "starcolor", true));
            }
            Collections.shuffle(this.getStack(), rng);
        }
    }

    /**
     * Quand la pioche est vide on la reconstruit avec les cartes de la defausse
     * @param defausse
     */
    public void DefausseInPioche(DefausseModel defausse) {
        if (this.stackSize() == 0) {
            while (defausse.stackSize() > 1) {
                CardModel carte = defausse.getCardAt(0);
                this.addCardAtTop(carte);
                defausse.getStack().remove(0);
            }
        }
        Collections.shuffle(this.getStack(), rng);
    }

}
