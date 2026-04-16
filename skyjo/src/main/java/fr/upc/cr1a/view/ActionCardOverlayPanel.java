package fr.upc.cr1a.view;

import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.AnimLoop;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Overlay affichant les cartes d'action en un grid
 * S'affiche au-dessus du plateau avec un fond semi-transparent
 */
public class ActionCardOverlayPanel extends JPanel {

    private JPanel cardsPanel;
    private CardButton[] actionCardButtons;
    private JPanel[] cardWrappers;
    private ActionType[] actionCardTypes;
    private int visibleActionCount = 0;
    private int selectedCardIndex = -1;
    private int hoveredCardIndex = -1;
    private GameBoardView gameBoardView;

    private float opaque = 0.0f;
    private Runnable onActionSelected = null;

    private static final int MAX_ACTION_CARDS = 8;
    private static final Color SEMI_TRANSPARENT_BG = new Color(0, 0, 0, 100);
    private JButton validateButton;

    /**
     * classe interne pour les panels de cartes sélectionnables avec contour arrondi transparent
     */
    private static class SelectableCardPanel extends JPanel {
        private boolean isSelected = false;

        public SelectableCardPanel() {
            super(new BorderLayout());
            setOpaque(false);
        }

        public void setSelectedStyle(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 200)); // Contour noir transparent
                g2.setStroke(new java.awt.BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                g2.dispose();
            }
        }
    }

    public ActionCardOverlayPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setVisible(false);

        //panel contenant les cartes avec fond blanc
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                //fond blanc semi-transparent
                g2.setColor(new Color(255, 255, 255, 220)); // Blanc transparent
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardsPanel.setOpaque(false);
        cardsPanel.setBackground(new Color(255, 255, 255, 220));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Pré-créer les boutons action
        actionCardButtons = new CardButton[MAX_ACTION_CARDS];
        cardWrappers = new JPanel[MAX_ACTION_CARDS];
        actionCardTypes = new ActionType[MAX_ACTION_CARDS];
        for (int i = 0; i < MAX_ACTION_CARDS; i++) {
            final int cardIndex = i;

            // Créer le wrapper panel
            SelectableCardPanel wrapper = new SelectableCardPanel();
            wrapper.setVisible(false);
            wrapper.setPreferredSize(new java.awt.Dimension(120, 168)); //taille de la carte actuon dans l'overlay

            CardButton btn = new CardButton();
            btn.setAnimationEnabled(false);
            btn.setVisible(false);
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectCard(cardIndex);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (cardIndex >= visibleActionCount) {
                        return;
                    }
                    hoveredCardIndex = cardIndex;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoveredCardIndex == cardIndex) {
                        hoveredCardIndex = -1;
                    }
                }
            });

            wrapper.add(btn, BorderLayout.CENTER);
            actionCardButtons[i] = btn;
            cardWrappers[i] = wrapper;
            cardsPanel.add(wrapper);
        }

        // Bouton Valider
        validateButton = new JButton("Valider");
        validateButton.setVisible(false);
        validateButton.setFont(validateButton.getFont().deriveFont(16f));
        validateButton.addActionListener(e -> playSelectedCard());

        // Panel inférieur avec le bouton
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(validateButton);

        // Centrer le panel de cartes
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        centerContainer.add(cardsPanel, gbc);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        mainContainer.add(centerContainer, BorderLayout.CENTER);
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);

        add(mainContainer, BorderLayout.CENTER);

        // Ajouter listener pour fermer l'overlay au clic en dehors des cartes
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Convertit les coordonnées de ce panel vers le cardsPanel
                Point pointInCards = SwingUtilities.convertPoint(
                    ActionCardOverlayPanel.this, e.getPoint(), cardsPanel);

                // Vérifie si le clic est en dehors des bounds du cardsPanel
                if (!cardsPanel.contains(pointInCards)) {
                    closeOverlay();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(SEMI_TRANSPARENT_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    /**
     * Sélectionne une carte d'action
     */
    private void selectCard(int index) {
        if (index >= visibleActionCount) return;

        selectedCardIndex = index;
        updateCardSelection();
    }

    /**
     * Met à jour l'affichage de la sélection
     */
    private void updateCardSelection() {
        for (int i = 0; i < MAX_ACTION_CARDS; i++) {
            SelectableCardPanel wrapper = (SelectableCardPanel) cardWrappers[i];
            wrapper.setSelectedStyle(i == selectedCardIndex);
        }

        // Affiche le bouton valider si une carte est sélectionnée
        validateButton.setVisible(selectedCardIndex >= 0);
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    /**
     * Joue la carte sélectionnée
     */
    private void playSelectedCard() {
        if (selectedCardIndex < 0 || selectedCardIndex >= visibleActionCount) return;

        ActionType selectedType = actionCardTypes[selectedCardIndex];
        if (selectedType == null) return;

        // Appelle le callback pour jouer la carte
        if (onActionSelected != null) {
            onActionSelected.run();
        }

        // Réinitialise la sélection et ferme
        selectedCardIndex = -1;
        validateButton.setVisible(false);
        closeOverlay();
    }

    /**
     * Met à jour et affiche les cartes action
     */
    public void showActionHand(java.util.ArrayList<ActionType> actions, GameBoardView gameBoardView) {
        this.gameBoardView = gameBoardView;
        visibleActionCount = (actions == null) ? 0 : actions.size();
        selectedCardIndex = -1;
        hoveredCardIndex = -1;

        for (int i = 0; i < MAX_ACTION_CARDS; i++) {
            if (i < visibleActionCount) {
                ActionType type = actions.get(i);
                actionCardTypes[i] = type;
                ActionCardModel tempCard = new ActionCardModel(false, type);
                String imagePath = gameBoardView.getCardImagePath(tempCard);
                actionCardButtons[i].setCardImage(imagePath);
                actionCardButtons[i].setVisible(true);
                actionCardButtons[i].setEnabled(true);
                cardWrappers[i].setVisible(true);
                cardWrappers[i].setBorder(null);
            } else {
                actionCardTypes[i] = null;
                actionCardButtons[i].clearImage();
                actionCardButtons[i].setVisible(false);
                actionCardButtons[i].setEnabled(false);
                cardWrappers[i].setVisible(false);
                cardWrappers[i].setBorder(null);
            }
        }

        validateButton.setVisible(false);
        cardsPanel.revalidate();
        cardsPanel.repaint();
        openOverlay();
    }

    /**
     * Affiche l'overlay avec animation
     */
    private void openOverlay() {
        AnimLoop.remove(this);
        setVisible(true);
        AnimLoop.add(this, dt -> {
            opaque += 0.12f * (float) dt;
            if (opaque >= 1.0f) {
                opaque = 1.0f;
                AnimLoop.remove(this);
            }
            repaint();
        });
    }

    /**
     * Ferme l'overlay avec animation
     */
    public void closeOverlay() {
        hoveredCardIndex = -1;
        AnimLoop.remove(this);
        AnimLoop.add(this, dt -> {
            opaque -= 0.12f * (float) dt;
            if (opaque <= 0.0f) {
                opaque = 0.0f;
                AnimLoop.remove(this);
                setVisible(false);
            }
            repaint();
        });
    }

    public CardButton[] getActionCardButtons() {
        return actionCardButtons;
    }

    public ActionType getActionCardType(int index) {
        return actionCardTypes[index];
    }

    public int getVisibleActionCount() {
        return visibleActionCount;
    }

    public int getSelectedCardIndex() {
        return selectedCardIndex;
    }

    public void setOnActionSelected(Runnable callback) {
        this.onActionSelected = callback;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.opaque));
        super.paint(g2);
        g2.dispose();
    }
}