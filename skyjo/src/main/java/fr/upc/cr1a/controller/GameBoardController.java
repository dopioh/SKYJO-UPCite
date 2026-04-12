package fr.upc.cr1a.controller;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import fr.upc.cr1a.controller.data.SendAPI;
import fr.upc.cr1a.model.*;
import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.model.player.PlayerData;
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

    private boolean timerEnabled = false;
    private GameLoop gameLoop;
    private HumanTurnController humanTurnController;

    private GameBoardBotController botController;

    private boolean waitingNextTurn = false; // empeche de cliquer pendant l'attente
    // pour fixer le clique du playerpreview
    private boolean isPinned = false;
    // panel de fin de manche
    RoundEndPanel currentRoundEndPanel = null;

    private boolean pauseOpen = false;
    private TimeoutTurnController timeoutTurnController;
    private boolean alreadyReward = false;

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

        //si tous les joueurs sont des bots, on n'installe pas les listeners
        if (!areAllPlayersBots()) {
            humanTurnController.installListeners();
        }
        
        gameBoardView.setTimerVisible(timerEnabled);
        gameLoop.setTimerEnabled(timerEnabled);

        InputMap im = gameBoardView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gameBoardView.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "togglePause");
        am.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePauseOverlay();
            }
        });

        gameBoardView.getPauseOverlay().getBtnResume().addActionListener(e -> closePauseOverlay());

        gameBoardView.getPauseOverlay().getBtnQuit().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(gameBoardModel.getFrame(),"Voulez-vous quitter la partie ?","Retour au menu",JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                closePauseOverlay();
                gameBoardModel.retourMenuCalled();
            }
        });

        gameBoardView.getPauseOverlay().getBtnSettings().addActionListener(e -> {
            gameBoardModel.getFrame().showSettingsOverlay();
        });

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
                public void mousePressed(MouseEvent e) {
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
        PlayerModel roundEndPlayer = gameManager.getRoundEndPlayer();

        currentRoundEndPanel = new RoundEndPanel(
            new ArrayList<>(gameManager.getPlayers()), isGameOver, roundEndPlayer
        );

        if (isGameOver) {
            rewardEndGame(currentRoundEndPanel.getSortedPlayers());
        }

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
        gameBoardView.setMancheLabel("Pré-Manche");
        waitingNextTurn = false;
        humanTurnController.resetTurnState();

        // Rafraîchir l'affichage du joueur actuel (y compris ses cartes actions)
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerCards(currentPlayer);

        // Forcer la mise à jour de l'affichage des cartes actions
        gameBoardView.showActionHand(currentPlayer);

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
        if (humanTurnController != null) {
            humanTurnController.removeAllListeners();
        }
        if (gameBoardView != null) {
            gameBoardView.cleanup();
        }
        AnimLoop.clearAll();
        setWaitingNextTurn(true);
    }

    private void togglePauseOverlay() {
        if(currentRoundEndPanel != null && currentRoundEndPanel.isShowing()) return;
        pauseOpen = !pauseOpen;
        gameBoardView.showPauseOverlay(pauseOpen);
        setWaitingNextTurn(pauseOpen);
        gameLoop.setPaused(pauseOpen);
    }

    private void closePauseOverlay() {
        pauseOpen = false;
        gameBoardView.showPauseOverlay(false);
        gameLoop.setPaused(false);
    }

    public void toggleTimer(boolean enabled) { /// IMPORTANT ICI ICI ICI ICI plus tard pour les différents type de parties
        gameBoardView.setTimerVisible(enabled);
        gameLoop.setTimerEnabled(enabled);
    }

    //vérifie si tous les joueurs sont des bots
    private boolean areAllPlayersBots() {
        for (PlayerModel player : gameManager.getPlayers()) {
            if (!(player instanceof BotModel)) {
                return false;
            }
        }
        return true;
    }


    private String getOpponentType(PlayerModel opponent) {
        if (!(opponent instanceof BotModel bot)) {
            return "human";
        }

        String strategyName = bot.getStrategy() == null ? "" : bot.getStrategy().getClass().getSimpleName().toLowerCase();
        if (strategyName.contains("count")) {
            return "bot_count";
        }
        if (strategyName.contains("low")) {
            return "bot_low";
        }
        return "bot_row";
    }

    private List<String> buildOpponentTypes(List<PlayerModel> opponents) {
        List<String> opponentTypes = new ArrayList<>();
        if (opponents == null) {
            return opponentTypes;
        }

        for (PlayerModel opponent : opponents) {
            opponentTypes.add(getOpponentType(opponent));
        }
        return opponentTypes;
    }

    private void rewardEndGame(List<PlayerModel> listPlayerInGame) {
        if (alreadyReward) {
            return;
        }
        if (PlayerAccount.actuelAccount == null || listPlayerInGame == null || listPlayerInGame.isEmpty()) {
            return;
        }

        PlayerModel dbPlayer = findConnectedDbPlayer(listPlayerInGame);
        if (dbPlayer == null) {
            System.out.println("elo et money non appliques car joueur DB introuvable");
            return;
        }

        alreadyReward = true;

        int rank = listPlayerInGame.indexOf(dbPlayer) + 1; // place du joueur db a la fin de la partie
        boolean wonGame = (rank == 1); // si rank = 1 alors jouer db a gagné

        List<PlayerModel> opponents = new ArrayList<>();
        for (PlayerModel player : gameManager.getPlayers()) {
            if (player != dbPlayer) {
                opponents.add(player);
            }
        }

        List<String> opponentTypes = buildOpponentTypes(opponents);

        String dbUuid = PlayerAccount.actuelAccount.getID();
        new Thread(() -> {
            PlayerData updated = SendAPI.applyGameResult(dbUuid, wonGame, opponentTypes);
            if (updated != null) {
                PlayerAccount.actuelAccount = updated;
                System.out.println("Fin de partie DB: " + (wonGame ? "WIN" : "PERDU"));
            } else {
                System.out.println("Fin de partie DB: echec applyGameResult");
            }
        }, "db-endgame-rewards").start();
    }

    // retrouve le joueur db dans la partie
    private PlayerModel findConnectedDbPlayer(List<PlayerModel> listPlayerInGame) {
        if (PlayerAccount.actuelAccount == null) {
            return null;
        }

        String dbId = PlayerAccount.actuelAccount.getID();
        String dbUsername = PlayerAccount.actuelAccount.getUsername();

        for (PlayerModel player : listPlayerInGame) {
            String name = player.getName();
            if (dbId != null && dbId.equals(name)) {
                return player;
            }
            if (dbUsername != null && !dbUsername.isBlank() && dbUsername.equalsIgnoreCase(name)) {
                return player;
            }
        }

        // si le joueur a un nom ingame different de la DB on prend le premier joueur humain ingame
        for (PlayerModel player : listPlayerInGame) {
            if (!(player instanceof BotModel)) {
                return (PlayerModel) player;
            }
        }
        return null;
    }
}
