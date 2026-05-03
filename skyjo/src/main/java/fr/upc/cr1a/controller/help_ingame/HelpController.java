package fr.upc.cr1a.controller.help_ingame;

import java.awt.Component;
import java.awt.Desktop.Action;

import fr.upc.cr1a.model.GameBoardModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GameManager.TurnPhase;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameBoardView;

//import java.awt.Frame;
import java.util.ArrayList;
import fr.upc.cr1a.view.help_ingame.HelpOverlay;

import fr.upc.cr1a.controller.HumanTurnController.ActionMode;

// il faut un model et un view aussi 
// les aides se feront via des fleches ? Fleches couleurs metaliques de preferences

public class HelpController {
    
    //private boolean isActivate = false;
    private Frame frame;
    private HelpOverlay helpOverlay;;

    public HelpController(Frame frame){
        this.frame = frame;
        this.helpOverlay = frame.getHelpOverlay();
    }

    public void chooseAndShow(){
        helpOverlay.clearHighlight();
        //System.out.println("[HelpController] ddebug 1");

        GameManager gameManager = frame.getCurrentGame();
        GameBoardView gameBoardView = gameManager.getGameBoardModel().getGameBoardView();
        if (gameManager == null || gameBoardView == null){return;}
        //System.out.println("[HelpController] ddebug 2");
        TurnPhase phase = gameManager.getTurnPhase();
        switch (phase) {
            case INIT_REVEAL:
                //helpOverlay.clearHighlight();
                Component[][] tmp8 = gameBoardView.getCardButtons();
                helpOverlay.addTargetComponent(tmp8); 
                break;
        
            case DRAW_DECISION : 
                ActionMode am = frame.getCurrentGame().getGameBoardModel().getGameBoardController().getHumanTurnController().getActionMode();
                //helpOverlay.clearHighlight();
                if (am != ActionMode.NONE){
                    switch (am) {
                    case SPY_ONE_COLUMN:
                        System.out.println("[HelpController] SPY_ONE_COLUMN ");
                        Component[][] a0 = gameBoardView.getCardButtons();
                        helpOverlay.addTargetComponent(a0);
                        break;
                    case REPLACE_CARD_FIRST:  
                        System.out.println("[ACTION_MODE] REPLACE_CARD_FIRST !!!!!!!!!!!!!");
                        Component[][] o0 = gameBoardView.getCardButtons();
                        helpOverlay.addTargetComponent(o0);
                        break;
                    case REPLACE_CARD_SECOND:
                        System.out.println("[ACTION_MODE] REPLACE_CARD_SECOND !!!!!!!!!!!!!!!!!!");
                        Component[][] o1 = gameBoardView.getCardButtons();
                        helpOverlay.addTargetComponent(o1);
                        break;
                    case TRADE_SELECT_OWN_CARD: 
                        System.out.println("[ACTION_MODE] TRADE_SELECT_OWN_CARD");
                        Component[][] a1 = gameBoardView.getCardButtons();
                        helpOverlay.addTargetComponent(a1);
                        break;
                    case TRADE_SELECT_TARGET_PLAYER:
                        System.out.println("[ACTION_MODE] TRADE_SELECT_TARGET_PLAYER");
                        helpOverlay.addTargetComponent(gameBoardView.getListPlayerButton(), gameManager.getCurrentPlayer().getName());
                        break;
                    case TRADE_SELECT_TARGET_CARD:
                        System.out.println("[ACTION_MODE] TRADE_SELECT_TARGET_CARD");
                        Component[][] a2 = gameBoardView.getCardButtons();
                        helpOverlay.addTargetComponent(a2);
                        break;
                    case ALL_PLAYERS_DROP_SELECT_TARGET:
                        System.out.println("[ACTION_MODE] ALL_PLAYERS_DROP_SELECT_TARGET");
                        helpOverlay.addTargetComponent(gameBoardView.getListPlayerButton(), gameManager.getCurrentPlayer().getName());
                        break;
                    case ALL_PLAYERS_DROP_SELECT_CARD:
                        System.out.println("[ACTION_MODE] ALL_PLAYERS_DROP_SELECT_CARD");
                        Component[][] a3 = gameBoardView.getCardButtons();
                        helpOverlay.addTargetComponent(a3);
                        break;
                    default:
                        System.out.println("[ACTION_MODE] default ? ");
                        break;
                    }
                    break;
                }
                //helpOverlay.clearHighlight();
                //System.out.println("[HelpController] case DRAW_DECISION de chooseAndShow activé");
                Component tmp = gameBoardView.getBtnPioche();
                Component tmp2 = gameBoardView.getBtnDefausse();
                helpOverlay.addTargetComponent(tmp);
                helpOverlay.addTargetComponent(tmp2);

                if ( gameManager.withActionCard()){
                    Component tmp3 = gameBoardView.getBtnPiocheAction();
                    //Component tmp4 = gameBoardView.getBtnDefausseAction(); // on peut pas piocher dans la defausse action 
                    helpOverlay.addTargetComponent(tmp3);
                    //helpOverlay.addTargetComponent(tmp4);

                    // main carte action
                    Component[] action = gameBoardView.getActionHandButtons();
                    helpOverlay.addTargetComponent(action);
                }
                break;
            case CARD_DRAW_FROM_DECK:
                //helpOverlay.clearHighlight();
                Component tmp5 = gameBoardView.getBtnDefausse();
                Component[][] tmp6 = gameBoardView.getCardButtons();

                helpOverlay.addTargetComponent(tmp5);
                helpOverlay.addTargetComponent(tmp6); // surcharge

                break;
            case CARD_TAKE_FROM_DISCARD:
                //helpOverlay.clearHighlight();
                Component[][] tmp7 = gameBoardView.getCardButtons();
                helpOverlay.addTargetComponent(tmp7); // surcharge
                break;
            // Où sont les phases des cartes actions ?
            default:
                //helpOverlay.clearHighlight();
                //System.out.println("[HelpController] ddebug 3");
                break;
        }
    }


    public void toggleHelp(boolean active) {
        GameManager gm = frame.getCurrentGame();
        if ( gm == null){return;}
        if (active) {
            this.helpOverlay.showPanel(true);
            chooseAndShow(); 
        } else {
            helpOverlay.showPanel(false);
            helpOverlay.clearHighlight(); 
        }
    }

    public HelpOverlay getHelpOverlay(){
        return this.helpOverlay;
    }
}
