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
    private Font doodle;

    public CreateLobbyDialog(Frame parent) {
        super(parent, "Créer un salon", true);
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodle = stream != null ? Font.createFont(Font.TRUETYPE_FONT, stream) : new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            doodle = new Font("Arial", Font.PLAIN, 12);
        }
        setSize(600, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(245, 245, 245));

        //titre
        JLabel titre = new JLabel("Créer un nouveau salon");
        titre.setFont(doodle.deriveFont(Font.BOLD, 24f));
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titre);
        mainPanel.add(Box.createVerticalStrut(20));

        //nom du salon
        JLabel nameLabel = new JLabel("Nom du salon:");
        nameLabel.setFont(doodle.deriveFont(Font.BOLD, 14f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lobbyNameField = new JTextField("Partie de SKYJO");
        lobbyNameField.setFont(doodle.deriveFont(Font.BOLD, 14f));
        lobbyNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lobbyNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(nameLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(lobbyNameField);
        mainPanel.add(Box.createVerticalStrut(20));

        //nombre de joueurs max
        JLabel playersLabel = new JLabel("Nombre maximum de joueurs:");
        playersLabel.setFont(doodle.deriveFont(Font.BOLD, 14f));
        playersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(4, 2, 8, 1);
        maxPlayersSpinner = new JSpinner(spinnerModel);
        maxPlayersSpinner.setFont(doodle.deriveFont(Font.BOLD, 14f));
        maxPlayersSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        maxPlayersSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(playersLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(maxPlayersSpinner);
        mainPanel.add(Box.createVerticalStrut(20));

        //mode actions (retire en multi online -- on garde l'attribut pour compat interne)
        actionModeCheckbox = new JCheckBox("Activer le mode Actions (cartes spéciales)");
        actionModeCheckbox.setSelected(false);
        actionModeCheckbox.setVisible(false); // masque en multi online
        mainPanel.add(Box.createVerticalStrut(20));

        //boutons en bas
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        cancelBtn = new DoodleButton("Annuler", ButtonColor.RED, 150, 50);
        cancelBtn.setFontSize(28f);
        createBtn = new DoodleButton("Créer", ButtonColor.GREEN, 150, 50);
        createBtn.setFontSize(28f);

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
        // J'ai enlevé le mode action en ligne , je laisse pour éviter un refactor
        return false;
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
