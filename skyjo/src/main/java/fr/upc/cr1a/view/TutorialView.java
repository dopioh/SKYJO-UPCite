package fr.upc.cr1a.view;

import fr.upc.cr1a.model.TutorialModel;
import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TutorialView extends JPanel {
    private TutorialModel model;
    private TriangleButton nextBtn;
    private TriangleButton prevBtn;
    private DoodleButton closeBtn;

    public TutorialView(TutorialModel model) {
        this.model = model;
        setOpaque(false);
        setLayout(null);
        
        // Boutons triangles
        prevBtn = new TriangleButton(false); // flèche gauche
        add(prevBtn);

        nextBtn = new TriangleButton(true); // flèche droite
        add(nextBtn);

        closeBtn = new DoodleButton("Fermer", ButtonColor.RED, 120, 50, "/Button/close.png");
        closeBtn.setFontSize(16f);
        add(closeBtn);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isClickInsideBox(e.getX(), e.getY())) {
                    e.consume();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isClickInsideBox(e.getX(), e.getY())) {
                    e.consume();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
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
        int boxHeight = 500;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;

        return x >= boxX && x <= boxX + boxWidth && y >= boxY && y <= boxY + boxHeight + 50;
    }

    //casse interne pour bouton triangle
    private static class TriangleButton extends JButton {
        private boolean isRight;

        public TriangleButton(boolean isRight) {
            this.isRight = isRight;
            setOpaque(true);
            setBackground(new Color(52, 152, 219));
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Dessiner le fond
            g2d.setColor(new Color(52, 152, 219));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Dessiner le triangle
            int[] xPoints, yPoints;
            if (isRight) {
                // Triangle pointant à droite
                xPoints = new int[] {5, 5, getWidth() - 5};
                yPoints = new int[] {5, getHeight() - 5, getHeight() / 2};
            } else {
                // Triangle pointant à gauche
                xPoints = new int[] {getWidth() - 5, getWidth() - 5, 5};
                yPoints = new int[] {5, getHeight() - 5, getHeight() / 2};
            }
            
            g2d.setColor(Color.WHITE);
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
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
        int boxHeight = 500;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;

        g2d.setColor(new Color(40, 40, 50));
        g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
        g2d.setColor(new Color(100, 150, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

        // Contenu du tutoriel
        g2d.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.PLAIN, 18);
        g2d.setFont(font);

        String content = model.getCurrentPageContent();
        String[] lines = content.split("\n");
        int y = boxY + 50;
        for (String line : lines) {
            g2d.drawString(line, boxX + 30, y);
            y += 30;
        }

        // Pagination en bas à droite
        String pagination = model.getCurrentPageNumber() + " / " + model.getTotalPages();
        Font paginationFont = new Font("Arial", Font.PLAIN, 14);
        g2d.setFont(paginationFont);
        FontMetrics fm = g2d.getFontMetrics();
        int paginationWidth = fm.stringWidth(pagination);
        g2d.drawString(pagination, boxX + boxWidth - paginationWidth - 30, boxY + boxHeight - 30);

        // Positionner les boutons triangles et fermer en bas à droite
        prevBtn.setBounds(boxX + boxWidth - 180, boxY + boxHeight - 70, 50, 50);
        nextBtn.setBounds(boxX + boxWidth - 120, boxY + boxHeight - 70, 50, 50);
        closeBtn.setBounds(boxX + (boxWidth - 120) / 2, boxY + boxHeight - 70, 120, 50);

        updateButtonStates();
    }

    private void updateButtonStates() {
        if (nextBtn != null) {
            nextBtn.setEnabled(model.hasNextPage());
        }
        if (prevBtn != null) {
            prevBtn.setEnabled(model.hasPreviousPage());
        }
    }

    public JButton getNextButton() {
        return nextBtn;
    }

    public JButton getPreviousButton() {
        return prevBtn;
    }

    public DoodleButton getCloseButton() {
        return closeBtn;
    }
}
