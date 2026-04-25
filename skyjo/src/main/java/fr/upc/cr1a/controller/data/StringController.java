package fr.upc.cr1a.controller.data;

public class StringController {

    /**
     * Classe utilitaire qui verifie que l'entrée n'est pas d'espace avant ou à la fin 
     * Et si oui, elle les enlèves
     * @param word
     * @return le paramètre propre
     */
    public static String withoutSpace(String word){

        while (word.startsWith(" ") || word.endsWith(" ")){
            if ( word.startsWith(" ")){
                word = word.substring(1);
                System.out.println("Suppression de l'espace au début : " + word);
            }
            if ( word.endsWith(" ")){
                word = word.substring(word.length() - 1, word.length());
                System.out.println("Suppression de l'espace à la fin : " + word);
            }
        }
        return word;
    }

}
