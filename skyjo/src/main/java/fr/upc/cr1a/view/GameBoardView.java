package fr.upc.cr1a.view;

import java.awt.*;
import javax.swing.*;

public class GameBoardView extends JPanel{

    public GameBoardView(){
        setLayout(new GridBagLayout());
        setOpaque(true);
        setVisible(true);
        initComponents();

        revalidate();
        repaint();
    }

    public void initComponents(){
        System.out.println("initComponents Called");
        
        JButton test = new JButton();
    }
}
