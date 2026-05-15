package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import java.awt.*;

public class TutorialModeSelectionView extends JPanel {
    private DoodleButton classicBtn;
    private DoodleButton actionBtn;

    public TutorialModeSelectionView() {
        setOpaque(false);
        setLayout(null);

        classicBtn = new DoodleButton("Skyjo Classique", ButtonColor.YELLOW, 350, 100, "/Button/Play.png");
        classicBtn.setFontSize(32f);
        classicBtn.setIconSide(DoodleButton.IconSide.LEFT);
        add(classicBtn);

        actionBtn = new DoodleButton("Skyjo Action", ButtonColor.GREEN, 350, 100, "/Button/Play_vert.png");
        actionBtn.setFontSize(32f);
        actionBtn.setIconSide(DoodleButton.IconSide.LEFT);
        add(actionBtn);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Overlay semi-transparent
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, width, height);

        // Cadre central
        int boxWidth = 700;
        int boxHeight = 400;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;

        g2d.setColor(new Color(40, 40, 50));
        g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
        g2d.setColor(new Color(100, 150, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

        // Titre
        g2d.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 32);
        g2d.setFont(font);
        String titre = "Choisissez votre tutoriel";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = boxX + (boxWidth - fm.stringWidth(titre)) / 2;
        g2d.drawString(titre, titleX, boxY + 60);

        // Positionner les boutons
        int btnWidth = 300;
        int btnHeight = 80;
        int spacing = 30;
        int totalWidth = (btnWidth * 2) + spacing;
        int startX = boxX + (boxWidth - totalWidth) / 2;
        int btnY = boxY + boxHeight - 150;

        classicBtn.setBounds(startX, btnY, btnWidth, btnHeight);
        actionBtn.setBounds(startX + btnWidth + spacing, btnY, btnWidth, btnHeight);

        // MouseListener modal
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!isClickInsideBox(e.getX(), e.getY())) {
                    e.consume();
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (!isClickInsideBox(e.getX(), e.getY())) {
                    e.consume();
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isClickInsideBox(e.getX(), e.getY())) {
                    e.consume();
                }
            }
        });
    }

    private boolean isClickInsideBox(int x, int y) {
        int width = getWidth();
        int height = getHeight();
        int boxWidth = 700;
        int boxHeight = 400;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;

        return x >= boxX && x <= boxX + boxWidth && y >= boxY && y <= boxY + boxHeight + 50;
    }

    public DoodleButton getClassicButton() {
        return classicBtn;
    }

    public DoodleButton getActionButton() {
        return actionBtn;
    }
}
