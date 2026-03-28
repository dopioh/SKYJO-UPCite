package fr.upc.cr1a.controller;

import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.ShopPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Controleur de la boutique.
 */
public class ShopController {

    private final Frame frame;
    private final ShopPanel view;

    public ShopController(Frame frame, ShopPanel view) {
        this.frame = frame;
        this.view = view;

        addButtonListeners(view.getBackButton(), () -> clicRetour());
        addButtonListeners(view.getClasseurButton(), () -> clicClasseur());

        DoodleButton[] buyButtons = view.getBuyButtons();
        for (int i = 0; i < buyButtons.length; i++) {
            int packNumber = i + 1;
            addButtonListeners(buyButtons[i], () -> clicAcheter(packNumber));
        }
    }

    private void addButtonListeners(DoodleButton button, Runnable action) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (view.isCaseOpeningVisible()) {
                    return;
                }
                button.onMouseEnter();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.onMouseExit();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (view.isCaseOpeningVisible()) {
                    return;
                }
                action.run();
            }
        });
    }

    private void clicRetour() {
        if (view.isCaseOpeningVisible()) {
            return;
        }
        frame.showPanel("MENU");
    }

    private void clicAcheter(int packNumber) {
        view.startCaseOpening(packNumber);
    }

    private void clicClasseur() {
        view.openClasseur();
    }
}
