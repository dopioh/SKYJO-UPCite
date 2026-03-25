package fr.upc.cr1a.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.PlayerData;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.PlayersSetupPanel;

public class PlayersSetupController {

    private final Frame frame;
    private final PlayersSetupPanel view;
    private final AudioManager audio;

    public PlayersSetupController(Frame frame, PlayersSetupPanel view) {
        this.frame = frame;
        this.view = view;
        this.audio = frame.getAudioManager();
        addRowHoverEffect();

        DoodleButton back = view.getBtnBack();
        back.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { back.onMouseEnter(); }
            @Override public void mouseExited(MouseEvent e)  { back.onMouseExit(); }
            @Override public void mousePressed(MouseEvent e) { frame.showPanel("GAME_MODE"); }
        });
        addListeners(view.getBtnLaunch(), () -> lancerPartie());

        view.getActionModeToggle().addActionListener(e -> {
            view.updateCardPreview(view.isSkyjoActionEnabled());
            System.out.println("Cartes action : " + view.isSkyjoActionEnabled());
        });

        for (Component comp : view.getPlayerCountButtonPanel().getComponents()) {
            if (comp instanceof JButton button) {
                button.addActionListener(e -> {
                    int value = Integer.parseInt(button.getActionCommand());
                    view.setSelectedPlayerCount(value);
                    view.rebuildPlayerRows(value);
                    updatePlayerCountButtonsStyle(value);
                    addRowHoverEffect();
                });
            }
        }
    }

    private void addRowHoverEffect() {
        Component[] rows = view.getPlayersContainer().getComponents();
        for (Component comp : rows) {
            if (!(comp instanceof JPanel row)) {
                continue;
            }
            JCheckBox botCheck = null;
            for (Component c : row.getComponents()) {
                if (c instanceof JCheckBox) {
                    botCheck = (JCheckBox) c;
                    break;
                }
            }
            if (botCheck == null) {
                continue;
            }
            JCheckBox finalBotCheck = botCheck;
            row.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (finalBotCheck.isSelected()) {
                        row.setBackground(new Color(220, 235, 255)); // hover bot
                    } else {
                        row.setBackground(new Color(240, 240, 240)); // hover joueur
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (finalBotCheck.isSelected()) {
                        row.setBackground(new Color(230, 242, 255)); // bot
                    } else {
                        row.setBackground(new Color(255, 255, 255, 220)); // joueur
                    }
                }
            });
        }
    }

    private void updatePlayerCountButtonsStyle(int selectedValue) {
        for (Component comp : view.getPlayerCountButtonPanel().getComponents()) {
            if (comp instanceof JButton button) {
                int value = Integer.parseInt(button.getActionCommand());

                if (value == selectedValue) {
                    button.setBackground(new Color(76, 175, 80));
                } else {
                    button.setBackground(new Color(255, 255, 255, 80));
                }
            }
        }
    }

    private void addListeners(DoodleButton btn, Runnable action) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.onMouseEnter();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.onMouseExit();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                audio.playSfx("/sfx/UIclick.wav");
                action.run();
            }
        });
    }

    private void lancerPartie() {
        List<PlayerData> players = new ArrayList<>();
        List<String> usedNames = new ArrayList<>();
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
                if (name == null || name.isBlank()) {
                    JOptionPane.showMessageDialog(frame,"Tous les joueurs doivent avoir un nom.","Nom manquant",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (usedNames.contains(name)) {
                    JOptionPane.showMessageDialog(frame,"Deux joueurs ne peuvent pas avoir le même nom","Nom en double",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                usedNames.add(name);
                players.add(new PlayerData(name));
            }
        }

        
        boolean actionMode = view.isSkyjoActionEnabled();
        new GameManager(frame, players, actionMode);
    }
}