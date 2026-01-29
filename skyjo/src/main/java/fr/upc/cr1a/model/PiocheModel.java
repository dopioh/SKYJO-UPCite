package fr.upc.cr1a.model;

import java.util.Collections;

/**
 * @author Nassim
 * @extends la classe CardStackModel
 * Classe permettant de créer des pioches de jeu (pile de 150 cartes)
 */
public class PiocheModel extends CardStackModel {

    public PiocheModel() {
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
                this.Stack.add(new NumberCardModel(true, value, color));
            }

            Collections.shuffle(this.Stack);
        }
    }

}
