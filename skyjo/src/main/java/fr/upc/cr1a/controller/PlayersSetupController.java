package fr.upc.cr1a.controller;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.PlayerData;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.PlayersSetupPanel;

public class PlayersSetupController {

    private final Frame frame;
    private final PlayersSetupPanel view;

    public PlayersSetupController(Frame frame, PlayersSetupPanel view) {
        this.frame = frame;
        this.view = view;
        addRowHoverEffect();
        view.getBtnBack().addActionListener(e -> frame.showPanel("MENU"));

        view.getBtnLaunch().addActionListener(e -> {
            int nb = (int) view.getSpinner().getValue();
            List<PlayerData> players = new ArrayList<>();
            int botCounter = 0;
            Component[] rows = view.getPlayersContainer().getComponents();

            for (Component comp : rows) {
                if (!(comp instanceof JPanel)) {
                    continue;
                }
                JPanel row = (JPanel) comp;
                JTextField nameField = null;
                JCheckBox botCheck = null;
                JComboBox<?> botType = null;

                for (Component c : row.getComponents()) {
                    if (c instanceof JTextField) nameField = (JTextField) c;
                    if (c instanceof JCheckBox) botCheck = (JCheckBox) c;
                    if (c instanceof JComboBox<?>) botType = (JComboBox<?>) c;
                }

                if (botCheck.isSelected()) {
                    botCounter++;
                    String botName = "Bot" + botCounter;
                    String type = (String) botType.getSelectedItem();
                    players.add(new PlayerData(botName, true, type));
                } else {
                    String name = nameField.getText().trim();
                    players.add(new PlayerData(name));
                }
            }
            new GameManager(frame, players);

            //------------------------------------------------------------ Faire un truc de ce style pour activer ou non (adapte en fonction de comment toi t'as fait)
            // boolean actionMode = view.isSkyjoActionEnabled();
            // new GameManager(frame, players, actionMode);
            
            
            frame.showPanel("GAME");
        });

        view.getSpinner().addChangeListener(e -> {
            int value = (int) view.getSpinner().getValue();
            view.rebuildPlayerRows(value);
            addRowHoverEffect();
        });

        view.getActionModeToggle().addActionListener(e -> {
            //Nassim Ici
        });
    }

    private void addRowHoverEffect() {
        Component[] rows = view.getPlayersContainer().getComponents();
        for (Component comp : rows) {
            if (!(comp instanceof JPanel row)) {
                continue;
            }
            Color normalColor = new Color(245, 245, 245);
            Color hoverColor = new Color(230, 230, 230);
            row.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    row.setBackground(hoverColor);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    row.setBackground(normalColor);
                }
            });
        }
    }
}