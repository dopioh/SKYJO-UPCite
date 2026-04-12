package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;

/**
 * Dialog personnalisée pour afficher les erreurs API/serveur
 * 
 */
public class ErrorDialog extends JDialog {

    private static final Color OVERLAY_BG = new Color(0, 0, 0, 180);
    private static final Color CARD_BG = new Color(248, 243, 226, 255);
    private static final Color CARD_BORDER = new Color(54, 45, 34, 215);
    private static final Color TITLE_COLOR = new Color(46, 38, 26);
    private static final Color TEXT_COLOR = new Color(80, 70, 50);

    private DoodleButton okButton;

    public ErrorDialog(Frame owner, String title, String message) {
        super(owner, title, true);
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setSize(500, 280);
        setLocationRelativeTo(owner);
        
        //panel principal avec fond semi-transparent
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                //fond semi-transparent noir (overlay)
                g2.setColor(OVERLAY_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                //carte blanche arrondie
                g2.setColor(CARD_BG);
                g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 20, 20);
                
                //bordure
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 20, 20);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());
        
        //panel titre
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(loadFont("/PatrickHandSC.ttf", 28f).deriveFont(Font.BOLD));
        titleLabel.setForeground(TITLE_COLOR);
        titlePanel.add(titleLabel);
        
        //panel message
        JPanel messagePanel = new JPanel();
        messagePanel.setOpaque(false);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        
        JTextArea messageArea = new JTextArea(message);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(loadFont("/GoogleSans-Regular.ttf", 16f));
        messageArea.setForeground(TEXT_COLOR);
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.add(messageArea);
        
        // panel bouton
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        okButton = new DoodleButton("OK", ButtonColor.GREEN, 140, 50);
        okButton.setFontSize(22f);
        okButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { okButton.onMouseEnter(); }
            @Override public void mouseExited(MouseEvent e) { okButton.onMouseExit(); }
            @Override public void mousePressed(MouseEvent e) { dispose(); }
        });
        buttonPanel.add(okButton);
        
        //assembly
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private Font loadFont(String path, float size) {
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
            }
        } catch (Exception ignored) {}
        return new Font("Arial", Font.PLAIN, (int) size);
    }

    public static void showError(Frame owner, String title, String message) {
        ErrorDialog dialog = new ErrorDialog(owner, title, message);
        dialog.setVisible(true);
    }
}
