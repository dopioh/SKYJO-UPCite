package fr.upc.cr1a.view.help_ingame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class HelpOverlay extends JPanel{
    // sera un overlay qui ne prendra en compte aucun clic
    // il affichera juste les actions que le joeur peut faire selon l'enum du tour 

    private boolean isVisible = false;

    private ArrayList<Component> targetComponents = new ArrayList<>(); // pour qu'on puisse avoir plusieurs highlight en meme temps 

    private boolean isHighlighting = false;

    public HelpOverlay(){
        setOpaque(false);
        //setBackground(java.awt.Color.RED);
        //setBounds(50, 50, 400, 400);
        setVisible(isVisible);
        //setLayout(new BorderLayout());
    }
    /**
     * Methode pour afficher (ou non) l'Overlay
     */
    public void showPanel(boolean b){
        isVisible = b;
        setVisible(isVisible);
    }

    // highlight le companent donnée 
    public void addTargetComponent(Component target) {
        if (target != null && target.isShowing()) {
            this.targetComponents.add(target);
            this.isHighlighting = true;
            this.revalidate(); 
            this.repaint();    
        }
    }
    public void addTargetComponent(Component[] target) {
        if ( target == null ) return;
        for ( int i = 0 ; i < target.length ; i++){
            this.targetComponents.add(target[i]);
            this.isHighlighting = true;
        }
        this.revalidate();  
        this.repaint();   
    }

    public void addTargetComponent(Component[][] target) { // surcharge pour la main de cardButtons
        if ( target == null ) return;
        for ( int i = 0 ; i < target.length ; i++){
            for (int j = 0 ; j < target[i].length ; j++){
                this.targetComponents.add(target[i][j]);
                this.isHighlighting = true;
            }
        }
        this.revalidate(); // pas besoin de recalculer à chaque fois 
        this.repaint(); 
    }

    // On efface bien toute la liste !
    public void clearHighlight() {
        this.targetComponents.clear();
        this.isHighlighting = false;
        this.revalidate();
        this.repaint();
    }

    //public void showHelpINIT_REVEAL(){}

    //public void showHelpDRAW_DECISION(){}

    //public void showHelpCARD_DRAW(){
        // si il pioche de la pioche : deux choix possibles -> echanger ou jeter
        // si il pioche de la defausse : 1 choix possible -> echanger
    //}

    @Override
    protected void paintComponent(Graphics g) {
        // C'est vital de garder le super.paintComponent
        super.paintComponent(g);
        
        if (!isVisible) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isHighlighting && !targetComponents.isEmpty()) {
            Area backgroundArea = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
            
            
            for (Component target : targetComponents) { 
                if (target.isShowing()) {

                    Point pt = SwingUtilities.convertPoint(target, 0, 0, this);
                    
                    int holeCenterX = pt.x + target.getWidth() / 2;
                    int holeCenterY = pt.y + target.getHeight() / 2;
                    int holeRadius = Math.max(target.getWidth(), target.getHeight()) / 2 + 30; 
                    
                    Ellipse2D.Double hole = new Ellipse2D.Double(
                        holeCenterX - holeRadius, 
                        holeCenterY - holeRadius, 
                        holeRadius * 2, holeRadius * 2
                    );
                    
                    backgroundArea.subtract(new Area(hole));
                    //g2d.setColor(new Color(0, 0, 90, 30)); pour colorer les highlight 
                    //g2d.fill(hole);
                }
            }
            
            g2d.setColor(new Color(0, 0, 0, 90)); // voile gris transparant 
            g2d.fill(backgroundArea);

        } else {
            g2d.setColor(new Color(0, 0, 0, 90)); // ou on le met à 0 quand il y'a pas de highlight ? 
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.dispose();
    }

    public boolean isActive(){
        return isVisible;
    }

}
