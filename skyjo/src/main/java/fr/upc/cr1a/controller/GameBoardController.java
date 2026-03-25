package fr.upc.cr1a.controller;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import fr.upc.cr1a.model.*;
import fr.upc.cr1a.view.*;

/**
 * Controller pour gerer les connections entre la view et le model
 * Du plateau de jeu
 * 
 * @see GameBoardView vue graphique
 * @see GameBoardModel logique du plateau
 * 
 */
public class GameBoardController {

    GameBoardModel gameBoardModel;
    GameBoardView gameBoardView;
    GameManager gameManager;

    private GameLoop gameLoop;
    private HumanTurnController humanTurnController;

    private GameBoardBotController botController;

    private boolean waitingNextTurn = false; // empeche de cliquer pendant l'attente
    // pour fixer le clique du playerpreview
    private boolean isPinned = false;
    // panel de fin de manche
    RoundEndPanel currentRoundEndPanel = null;

    private TimeoutTurnController timeoutTurnController;

    /**
     * Initialise la view et le Model du GameBoard
     */
    public GameBoardController(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameManager gameManager) {
        this.gameBoardModel = gameBoardModel;
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.botController = new GameBoardBotController(gameBoardView, gameManager, this);

        this.gameLoop = new GameLoop(gameManager, gameBoardView);
        this.gameLoop.setHumanController(this);
        this.gameLoop.setBotController(botController);

        this.timeoutTurnController = new TimeoutTurnController(gameBoardView, gameManager, this, gameLoop);
        this.gameLoop.setTimeoutController(timeoutTurnController);

        this.humanTurnController = new HumanTurnController(gameBoardView, gameManager, this, gameLoop);
        humanTurnController.installListeners();
        
        listener();
        refreshGuide();
        
        gameBoardView.setActionDecksVisible(gameManager.withActionCard());
        
        // Initialiser l'affichage des pioches (normales + actions)
        updateAllDecksDisplay();
        
        //vérifie si c'est un bot
        SwingUtilities.invokeLater(() -> gameLoop.startGameLoop());
    }

    /**
     * les listeners du plateau :
     * - bouton retour vers le menu
     * - clic sur les cartes affichées
     * 
     * Les boutons joueur a gauche ne changent plus la main affichée mais
     * la fonction buttonPlayerCalled existe toujours dans le model si
     * on a besoin de changer la main affichée a droite
     */
    public void listener() {

        JButton retour = gameBoardView.getBtnRetour();
        retour.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(gameBoardModel.getFrame(),"Voulez-vous quitter la partie ?","Retour au menu",JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                gameBoardModel.retourMenuCalled();
            }
        });

        // Pour le survol et le ckick des preview player
        // y'a un soucis pour les pinned ici -> bug mineur
        int s = 0;
        for (JButton jb : gameBoardView.getListPlayerButton()) {
            int tmp = s;
            jb.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (tmp == gameManager.getCurrentPlayerIndex()) {
                        return;
                    }
                    gameBoardModel.buttonPlayerCalled(tmp);
                    gameBoardView.showOtherPlayerMain(true);
                    isPinned = false;   
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (humanTurnController != null && humanTurnController.handlePlayerButtonClicked(tmp)) {
                        return;
                    }
                    if (tmp == gameManager.getCurrentPlayerIndex()) {
                        return;
                    }
                    gameBoardModel.buttonPlayerCalled(tmp);
                    gameBoardView.showOtherPlayerMain(true);
                    isPinned = false;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //gameBoardView.setIsPlayer(false);
                    if (!isPinned) {
                        gameBoardView.showOtherPlayerMain(false);
                        isPinned = true;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!isPinned) {
                        gameBoardView.showOtherPlayerMain(false);
                        isPinned = true;
                    }
                }
            });
            s++;
        }

    }

    /**
     * Affiche l'ecran de fin de manche avec le classement et le bouton continuer/menu
     */
    private void showRoundEndScreen() {
        Frame frame = gameBoardModel.getFrame();
        if (currentRoundEndPanel != null) {
            frame.remove(currentRoundEndPanel);
        }

        boolean isGameOver = gameManager.getGameState() == GameManager.GameState.GAME_ENDED;
        currentRoundEndPanel = new RoundEndPanel(
            new ArrayList<>(gameManager.getPlayers()), isGameOver
        );

        frame.add(currentRoundEndPanel, "ROUND_END");
        frame.showPanel("ROUND_END");

        // hover sur les joueurs -> preview de leur main
        List<JLabel> labels = currentRoundEndPanel.getPlayerLabels();
        List<PlayerModel> sortedPlayers = currentRoundEndPanel.getSortedPlayers();
        for (int i = 0; i < labels.size(); i++) {
            JLabel label = labels.get(i);
            PlayerModel player = sortedPlayers.get(i);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setBackground(new Color(242, 242, 242));
                    currentRoundEndPanel.showPreview(player);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    label.setBackground(Color.WHITE);
                    currentRoundEndPanel.hidePreview();
                }
            });
        }

        // bouton Continuer
        if (!isGameOver && currentRoundEndPanel.getBtnContinuer() != null) {
            currentRoundEndPanel.getBtnContinuer().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { currentRoundEndPanel.getBtnContinuer().onMouseEnter(); }
                @Override
                public void mouseExited(MouseEvent e) { currentRoundEndPanel.getBtnContinuer().onMouseExit(); }
                @Override
                public void mousePressed(MouseEvent e) { ContinueRound(); }
            });
        }

        // bouton Menu
        if (isGameOver && currentRoundEndPanel.getBtnRetourMenu() != null) {
            currentRoundEndPanel.getBtnRetourMenu().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { currentRoundEndPanel.getBtnRetourMenu().onMouseEnter(); }
                @Override
                public void mouseExited(MouseEvent e) { currentRoundEndPanel.getBtnRetourMenu().onMouseExit(); }
                @Override
                public void mousePressed(MouseEvent e) { gameBoardModel.retourMenuCalled(); }
            });
        }
    }

    /**
     * Relance une nouvelle manche apres clic sur Continuer
     */
    private void ContinueRound() {
        gameManager.startNewGame();
        waitingNextTurn = false;
        humanTurnController.resetTurnState();
        gameBoardView.showPlayerCards(gameManager.getCurrentPlayer());
        updateAllDecksDisplay();
        Frame frame = gameBoardModel.getFrame();
        frame.showPanel("GAME");
        refreshGuide();
        SwingUtilities.invokeLater(() -> gameLoop.startGameLoop());
    }

    /**
     * Message d'action indiqunat au joueur quoi faire
     */
    public void refreshGuide() {
        switch (gameManager.getTurnPhase()) {
            case DRAW_DECISION -> gameBoardView.setActionGuide("Choisis : Pioche ou Défausse");
            case CARD_DRAW_FROM_DECK -> gameBoardView.setActionGuide("Echange ou Défausse puis revele une carte");
            case CARD_TAKE_FROM_DISCARD -> gameBoardView.setActionGuide("Clique une carte de ta main pour échanger");
            case CARD_DISCARD_FROM_DRAW -> gameBoardView.setActionGuide("Veuillez reveler une carte de votre main");
            case TURN_ENDED -> gameBoardView.setActionGuide("Tour terminé");
            case INIT_REVEAL -> gameBoardView.setActionGuide(gameManager.getCurrentPlayer().getName()
                    + " doit réveler 2 cartes (" + gameManager.getInitRevealCount() + ")");
        }
    }

    /**
     * getter/setter pour waitingNextTurn
     */
    public void setWaitingNextTurn(boolean waiting) {
        this.waitingNextTurn = waiting;
    }

    public boolean isWaitingNextTurn() {
        return waitingNextTurn;
    }

    /**
     * Met a jour l'affichage des pioches et défausses
     */
    public void updateAllDecksDisplay() {
        // maj normales
        gameBoardView.updateCardDisplay(
            gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard(),
            gameManager.getDeck().stackSize() > 0
        );

        // maj actions
        if (gameManager.withActionCard() && gameManager.getDiscardActionPile() != null && gameManager.getActionDeck() != null) {
            gameBoardView.updateActionCardDisplay(
                gameManager.getDiscardActionPile().isEmpty() ? null : gameManager.getDiscardActionPile().getTopCard(),
                gameManager.getActionDeck().stackSize() > 0
            );
        }
    }

    public void requestEndTurnFromBot() {
        gameLoop.proceedAfterAction();
    }

    public void showRoundEndScreenFromLoop() {
        showRoundEndScreen();
    }

    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stopGame();
        }
        setWaitingNextTurn(true);
    }
}
