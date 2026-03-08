package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;

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

        JPanel configTop = new JPanel();
        configTop.setOpaque(false);
        configTop.add(new JLabel("Nombre de joueurs : "));
        spinner = new JSpinner(new SpinnerNumberModel(2, 2, 8, 1));

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

        // Panel a droite du container
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel optionsTitle = new JLabel("Options");
        optionsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        optionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionModeToggle = new JToggleButton("Mode Skyjo Action");
        actionModeToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionModeToggle.setFocusPainted(false);

        optionsPanel.add(optionsTitle);
        optionsPanel.add(Box.createVerticalStrut(15));
        optionsPanel.add(actionModeToggle);
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
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setOpaque(false);

            JLabel label = new JLabel("Joueur " + playerIndex + " : ");
            JTextField nameField = new JTextField(10);
            JCheckBox botCheck = new JCheckBox("Bot");
            JComboBox<String> botType = new JComboBox<>(new String[]{"bot", "botRow", "botLow", "botCounting"});
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
        }
        playersContainer.revalidate();
        playersContainer.repaint();
    }
}