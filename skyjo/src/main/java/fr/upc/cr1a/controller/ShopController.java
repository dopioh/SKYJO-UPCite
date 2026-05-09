package fr.upc.cr1a.controller;

import fr.upc.cr1a.controller.data.ApiErrorManager;
import fr.upc.cr1a.controller.data.SendAPI;
import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.ErrorDialog;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.ShopPanel;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Controleur de la boutique.
 */
public class ShopController {

    private final Frame frame;
    private final ShopPanel view;
    private final AudioManager audio;
    private boolean achatEnCours = false;

    public ShopController(Frame frame, ShopPanel view) {
        this.frame = frame;
        this.view = view;
        this.audio = frame.getAudioManager();

        addButtonListeners(view.getBackButton(), () -> clicRetour());
        addButtonListeners(view.getClasseurButton(), () -> clicClasseur());

        view.getClasseurButton().setSfxPath(null);
        DoodleButton[] buyButtons = view.getBuyButtons();
        for (int i = 0; i < buyButtons.length; i++) {
            int packNumber = i + 1;
            buyButtons[i].setSfxPath(null); // pour effacer le son par defaut de doodlebutton
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
            public void mousePressed(MouseEvent e) {
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
        if (view.isCaseOpeningVisible() || achatEnCours) {
            return;
        }

        int price = packNumber * 100;

        // en mode invité (on theorie il va servir a rien car tout sera debloqué mais j'ai pas encore fait ça)
        if (PlayerAccount.actuelAccount == null) {
            if (view.getMoney() < price) {
                return;
            }
            audio.playSfx("/sfx/money_buy.wav");
            view.startCaseOpening(packNumber);
            view.setMoney(view.getMoney() - price);
            view.refreshMoneyButton();
            return;
        }

        // mode joueur db
        if (PlayerAccount.actuelAccount.getMoney() < price) {
            return;
        }

        achatEnCours = true;
        final String uuid = PlayerAccount.actuelAccount.getID();
        final String packName = "pack_numero_" + packNumber;
        new Thread(() -> {
            try {
                PlayerData majdata = SendAPI.buyPack(uuid, packName);
                if (majdata == null) {
                    SwingUtilities.invokeLater(() -> ErrorDialog.showError(
                        frame,
                        "Boutique",
                        ApiErrorManager.getLastUserMessageOrDefault("Le pack ne peut pas etre achete maintenant. Retente juste quand t'auras de la tune.")
                    ));
                    return;
                }
                PlayerAccount.actuelAccount = majdata;
                SwingUtilities.invokeLater(() -> {
                    audio.playSfx("/sfx/money_buy.wav");
                    view.startCaseOpening(packNumber);
                    view.setMoney(majdata.getMoney());
                    view.refreshMoneyButton();
                });
            } finally {
                achatEnCours = false;
            }
        }, "shop-buy-pack").start();
    }

    private void clicClasseur() {
        audio.playSfx("/sfx/classeur.wav");
        view.openClasseur();
    }
}
