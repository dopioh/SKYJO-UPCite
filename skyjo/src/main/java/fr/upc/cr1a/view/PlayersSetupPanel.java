package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlayersSetupPanel extends JPanel {

    private final JButton btnBack;
    private final JButton btnLaunch;
    private JSpinner spinner;
    private JPanel playersContainer;
    private JToggleButton actionModeToggle;

    public PlayersSetupPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        //Partie du hauit
        JLabel title = new JLabel("Configuration des joueurs", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        center.setOpaque(false);

        JPanel configTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        configTop.setOpaque(false);
        configTop.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JLabel playerCountLabel = new JLabel("Nombre de joueurs (1 à 8) :");
        playerCountLabel.setFont(new Font("Arial", Font.BOLD, 15));

        spinner = new JSpinner(new SpinnerNumberModel(2, 2, 8, 1));
        spinner.setPreferredSize(new Dimension(70, 32));
        spinner.setFont(new Font("Arial", Font.BOLD, 14));
        spinner.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190), 1));

        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        JFormattedTextField textField = editor.getTextField();
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setFont(new Font("Arial", Font.BOLD, 14));
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(Color.WHITE);
        for(Component comp : spinner.getComponents()){
            if (comp instanceof JButton button) {
                button.setFocusPainted(false);
                button.setBackground(new Color(245, 245, 245));
                button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }

        configTop.add(playerCountLabel);
        configTop.add(spinner);
        center.add(configTop, BorderLayout.NORTH);

        // Partie centrale
        playersContainer = new JPanel();

        // Container (gauche) 
        playersContainer = new JPanel();
        playersContainer.setLayout(new BoxLayout(playersContainer, BoxLayout.Y_AXIS));
        playersContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(playersContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Panel a droite du container
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(true);
        optionsPanel.setBackground(new Color(245, 245, 245));
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200)),
                BorderFactory.createEmptyBorder(15,15,15,15)
        ));

        JLabel optionsTitle = new JLabel("Options");
        optionsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        optionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionModeToggle = new JToggleButton("Mode Skyjo Action");
        actionModeToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionModeToggle.setFocusPainted(false);

        JLabel actionDescription = new JLabel("Ajoute des cartes spéciales et des effets supplémentaires");
        actionDescription.setFont(new Font("Arial", Font.PLAIN, 12));
        actionDescription.setForeground(new Color(90, 90, 90));
        actionDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        optionsPanel.add(optionsTitle);
        optionsPanel.add(Box.createVerticalStrut(15));
        optionsPanel.add(actionModeToggle);
        optionsPanel.add(Box.createVerticalStrut(8));
        optionsPanel.add(actionDescription);
        optionsPanel.add(Box.createVerticalGlue());

        // Trait de separtation
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scroll,optionsPanel);

        splitPane.setResizeWeight(0.7);     
        splitPane.setDividerLocation(0.5);  
        splitPane.setDividerSize(2);
        splitPane.setBorder(null);

        center.add(splitPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Partie du bas
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottom.setOpaque(false);

        btnBack = new JButton("Retour");
        btnLaunch = new JButton("Lancer");

        //UX Bouton , a modifier plus tard pour le lier avec la DA du reste
        btnBack.setPreferredSize(new Dimension(120, 36));
        btnLaunch.setPreferredSize(new Dimension(140, 36));
        btnBack.setFont(new Font("Arial", Font.PLAIN, 14));
        btnLaunch.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.setFocusPainted(false);
        btnLaunch.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLaunch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setBackground(new Color(240, 240, 240));
        btnLaunch.setBackground(new Color(230, 230, 230));

        bottom.add(btnBack);
        bottom.add(btnLaunch);

        add(bottom, BorderLayout.SOUTH);

        rebuildPlayerRows(2);
    }

    public JButton getBtnBack() {
        return btnBack;
    }

    public JButton getBtnLaunch() {
        return btnLaunch;
    }

    public JSpinner getSpinner() {
        return spinner;
    }

    public JPanel getPlayersContainer() {
        return playersContainer;
    }

    public JToggleButton getActionModeToggle() {
        return actionModeToggle;
    }

    public boolean isSkyjoActionEnabled() {
        return actionModeToggle.isSelected();
    }
    
    /**
     * Sert a refaire le container de la partie centrale en fonction du nombre de joueurs 
     * @param count nombre de joueurs
     */
    public void rebuildPlayerRows(int count) {
        playersContainer.removeAll();
        for (int i = 1; i <= count; i++) {
            final int playerIndex = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 11));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200,200,200)),
                    BorderFactory.createEmptyBorder(8,8,8,8)
            ));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            row.setOpaque(true);
            row.setBackground(new Color(245, 245, 245));

            JLabel label = new JLabel("Joueur " + playerIndex + " : ");
            
            JTextField nameField = new JTextField(12);
            nameField.setPreferredSize(new Dimension(120, 30));
            nameField.setFont(new Font("Arial", Font.PLAIN, 14));
            nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200,200,200)),
                    BorderFactory.createEmptyBorder(4,8,4,8)
            ));
            nameField.setText("Joueur " + playerIndex);
            nameField.setForeground(Color.BLACK);
            nameField.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (nameField.getText().equals("Joueur " + playerIndex)) {
                        nameField.setText("");
                    } else {
                        nameField.selectAll();
                    }
                }
            });

            JCheckBox botCheck = new JCheckBox("Bot");
            JComboBox<String> botType = new JComboBox<>(new String[]{"bot", "botRow", "botLow"});
            botType.setEnabled(false);

            botCheck.addActionListener(e -> {
                boolean isBot = botCheck.isSelected();
                nameField.setEnabled(!isBot);
                botType.setEnabled(isBot);
                if (isBot) {
                    nameField.setText("Bot" + playerIndex);
                } else {
                    nameField.setText("");
                }
            });
            row.add(label);
            row.add(nameField);
            row.add(botCheck);
            row.add(botType);
            playersContainer.add(row);
            playersContainer.add(Box.createVerticalStrut(2)); // Mini espacement
        }
        playersContainer.revalidate();
        playersContainer.repaint();
    }
}