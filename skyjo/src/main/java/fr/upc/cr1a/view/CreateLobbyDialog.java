package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import java.awt.*;

/**
 * dialog pour créer un nouveau salon multijoueur
 *
 */
public class CreateLobbyDialog extends JDialog {

    private JTextField lobbyNameField;
    private JSpinner maxPlayersSpinner;
    private JCheckBox actionModeCheckbox;
    private DoodleButton createBtn;
    private DoodleButton cancelBtn;
    private boolean confirmed = false;

    public CreateLobbyDialog(Frame parent) {
        super(parent, "Créer un salon", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(245, 245, 245));

        //titre
        JLabel titre = new JLabel("Créer un nouveau salon");
        titre.setFont(new Font("Arial", Font.BOLD, 24));
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titre);
        mainPanel.add(Box.createVerticalStrut(20));

        //nom du salon
        JLabel nameLabel = new JLabel("Nom du salon:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lobbyNameField = new JTextField("Partie de SKYJO");
        lobbyNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        lobbyNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lobbyNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(nameLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(lobbyNameField);
        mainPanel.add(Box.createVerticalStrut(20));

        //nombre de joueurs max
        JLabel playersLabel = new JLabel("Nombre maximum de joueurs:");
        playersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        playersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(4, 2, 8, 1);
        maxPlayersSpinner = new JSpinner(spinnerModel);
        maxPlayersSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        maxPlayersSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        maxPlayersSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(playersLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(maxPlayersSpinner);
        mainPanel.add(Box.createVerticalStrut(20));

        //mode actions
        actionModeCheckbox = new JCheckBox("Activer le mode Actions (cartes spéciales)");
        actionModeCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        actionModeCheckbox.setBackground(new Color(245, 245, 245));
        actionModeCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionModeCheckbox.setSelected(false);

        mainPanel.add(actionModeCheckbox);
        mainPanel.add(Box.createVerticalStrut(30));

        //boutons en bas
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        cancelBtn = new DoodleButton("Annuler", ButtonColor.RED, 150, 60);
        createBtn = new DoodleButton("Créer", ButtonColor.GREEN, 150, 60);

        buttonsPanel.add(cancelBtn);
        buttonsPanel.add(createBtn);

        mainPanel.add(buttonsPanel);

        add(mainPanel);
    }

    /**
     *retourne le nom du salon saisi
     */
    public String getLobbyName() {
        return lobbyNameField.getText().trim();
    }

    /**
     *retourne le nombre max de joueurs sélectionné
     */
    public int getMaxPlayers() {
        return (Integer) maxPlayersSpinner.getValue();
    }

    /**
     * retourne si le mode actions est activé
     */
    public boolean isActionMode() {
        return actionModeCheckbox.isSelected();
    }

    /**
     * retourne si l'utilisateur a confirmé la création
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * définit si la création est confirmée
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public DoodleButton getCreateButton() {
        return createBtn;
    }

    public DoodleButton getCancelButton() {
        return cancelBtn;
    }

    /**
     * valide les données saisies
     */
    public boolean validateInput() {
        String name = getLobbyName();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez entrer un nom pour le salon.",
                "Nom manquant",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        if (name.length() > 50) {
            JOptionPane.showMessageDialog(
                this,
                "Le nom du salon ne peut pas dépasser 50 caractères.",
                "Nom trop long",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        return true;
    }
}
