package fr.upc.cr1a.view.help_ingame;

import javax.swing.*;
import java.awt.*;

public class HelpOverlay extends JPanel{
    // sera un overlay qui ne prendra en compte aucun clic
    // il affichera juste les actions que le joeur peut faire selon l'enum du tour 

    public HelpOverlay(){
        setOpaque(false);
        setVisible(false);
        setLayout(new BorderLayout());
    }
    /**
     * Methode pour afficher (ou non) l'Overlay
     */
    public void showPanel(boolean b){
        setVisible(b);
    }

    public void showHelpINIT_REVEAL(){}

    public void showHelpDRAW_DECISION(){}

    public void showHelpCARD_DRAW(){
        // si il pioche de la pioche : deux choix possibles -> echanger ou jeter
        // si il pioche de la defausse : 1 choix possible -> echanger
    }

}
