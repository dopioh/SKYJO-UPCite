package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;
import fr.upc.cr1a.model.CardModel;

public class StackOverlay extends JPanel{
    private final CardButton drawButton = new CardButton();   
    private final CardButton discardButton = new CardButton();

    public StackOverlay(){
        setOpaque(false);
        setLayout(new GridBagLayout());

    }
}
