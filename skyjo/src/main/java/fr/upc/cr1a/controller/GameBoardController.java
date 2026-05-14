package fr.upc.cr1a.controller;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;

import javax.swing.*;

import fr.upc.cr1a.controller.data.ApiErrorManager;
import fr.upc.cr1a.controller.data.SendAPI;
import fr.upc.cr1a.controller.help_ingame.HelpController;
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

    private boolean timerEnabled =(GameSession.status == GameSession.STATUS.HOST|| GameSession.status == GameSession.STATUS.CLIENT);
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
    private AudioManager audio;

    private HelpController helpController;

    /**
     * Initialise la view et le Model du GameBoard
     */
    public GameBoardController(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameManager gameManager) {
        this.gameBoardModel = gameBoardModel;
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.helpController = gameManager.getHelpController();
        this.botController = new GameBoardBotController(gameBoardView, gameManager, this);

        this.gameLoop = new GameLoop(gameManager, gameBoardView);
        this.gameLoop.setHumanController(this);
        this.gameLoop.setBotController(botController);

        this.timeoutTurnController = new TimeoutTurnController(gameBoardView, gameManager, this, gameLoop);
        this.gameLoop.setTimeoutController(timeoutTurnController);
        
        this.humanTurnController = new HumanTurnController(gameBoardView, gameManager, this, gameLoop);
        humanTurnController.installListeners();
        humanTurnController.disableListenersIfOnlyBots();
        
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

        // Tab pour scoreboard
        installTabScoreboardDispatcher();

        audio = gameBoardModel.getFrame().getAudioManager();
        gameBoardView.getPauseOverlay().getBtnResume().addActionListener(e -> { audio.playSfx("/sfx/button_click.wav"); closePauseOverlay(); });

        gameBoardView.getPauseOverlay().getBtnQuit().addActionListener(e -> {
            audio.playSfx("/sfx/back.wav");
            int confirm = JOptionPane.showConfirmDialog(gameBoardModel.getFrame(),"Voulez-vous quitter la partie ?","Retour au menu",JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                closePauseOverlay();
                gameBoardModel.retourMenuCalled();
            }
        });

        gameBoardView.getPauseOverlay().getBtnSettings().addActionListener(e -> {
            audio.playSfx("/sfx/button_click.wav");
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
        for (DoodleButton jb : gameBoardView.getListPlayerButton()) {
            int tmp = s;
            jb.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (tmp == gameManager.getCurrentPlayerIndex()) {
                        return;
                    }
                    jb.onMouseEnter();
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
                    if (tmp != gameManager.getCurrentPlayerIndex()) {
                        jb.onMouseExit();
                    }
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
        PlayerModel lowestScorePlayer = fr.upc.cr1a.model.ScoreCalculator.getLowestScorePlayer(gameManager.getPlayers());
        currentRoundEndPanel = new RoundEndPanel(
            new ArrayList<>(gameManager.getPlayers()), isGameOver, gameManager.getLastRoundPlayer(), lowestScorePlayer
        );

        if (isGameOver) {
            rewardEndGame(currentRoundEndPanel.getSortedPlayers());
        }

        frame.add(currentRoundEndPanel, "ROUND_END");
        if (!isGameOver) audio.playBgm("/music/elevator_bgm.wav");
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

        // Le Host choisit de continuer la game (Ici) On peut aussi mettre un timer?
        // Les CLIENTS attendent UPDATE_NEW_ROUND du host.
        if (!isGameOver && currentRoundEndPanel.getBtnContinuer() != null) {
            if (GameSession.status == GameSession.STATUS.CLIENT) {
                currentRoundEndPanel.getBtnContinuer().setEnabled(false);
                currentRoundEndPanel.getBtnContinuer().setVisible(false);
            } else {
                currentRoundEndPanel.getBtnContinuer().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { currentRoundEndPanel.getBtnContinuer().onMouseEnter(); }
                    @Override
                    public void mouseExited(MouseEvent e) { currentRoundEndPanel.getBtnContinuer().onMouseExit(); }
                    @Override
                    public void mousePressed(MouseEvent e) { ContinueRound(); }
                });
            }
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
     * Relance une nouvelle manche apres clic sur Continuer.
     * En multi (HOST) : genere un seed et le broadcast pour synchroniser le deck.
     */
    private void ContinueRound() {
        long seed = new java.util.Random().nextLong();
        if (GameSession.status == GameSession.STATUS.HOST) {
            try {
                fr.upc.cr1a.controller.network.Host host = gameBoardModel.getFrame().getLobbyRoomController().getHost();
                if (host != null && host.getGameHost() != null) {
                    host.getGameHost().broadcastNewRound(seed);
                }
            } catch (Exception ignored) {}
        }
        startLocalNewRound(seed);
    }

    /**
     * Reset local pour une nouvelle manche
     */
    public void startLocalNewRound(long seed) {
        String[] bgms = {"/music/game_bgm1.wav", "/music/game_bgm2.wav"};
        audio.playBgm(bgms[(int)(Math.random() * 2)]);
        gameManager.startNewGame(seed);
        gameBoardView.setMancheLabel("Pré-Manche");
        waitingNextTurn = false;
        if (humanTurnController != null) humanTurnController.resetTurnState();
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
        // refresh l'aide si c'est activé
        if (this.helpController != null && this.helpController.getHelpOverlay().isActive()) {
            this.helpController.chooseAndShow();
        }
    }

    /**
     * getter/setter pour waitingNextTurn
     */
    public AudioManager getAudio() { return audio; }

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

    public GameLoop getGameLoop() {
        return gameLoop;
    }

    /**
     * Appele apres un TURN_CHANGED reseau : si on est host et que le nouveau joueur courant est un bot, lance son tour.
     */
    public void triggerBotTurnIfNeeded() {
        if (GameSession.status != GameSession.STATUS.HOST) return;
        PlayerModel cp = gameManager.getCurrentPlayer();
        if (!(cp instanceof BotModel)) return;
        if (botController == null) return;
        SwingUtilities.invokeLater(() -> botController.handleBotTurn());
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
        // Desinstaller le dispatcher Tab pour eviter qu'il reste actif dans le menu
        if (tabDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(tabDispatcher);
            tabDispatcher = null;
        }
        AnimLoop.clearAll();
        setWaitingNextTurn(true);
    }

    private void togglePauseOverlay() {
        if(currentRoundEndPanel != null && currentRoundEndPanel.isShowing()) return;
        pauseOpen = !pauseOpen;
        gameBoardView.showPauseOverlay(pauseOpen);
        // En multi : pause purement visuelle (overlay), on ne bloque pas les inputs
        // ni le timer (la partie continue pour les autres joueurs).
        boolean isMulti = (GameSession.status == GameSession.STATUS.HOST
                        || GameSession.status == GameSession.STATUS.CLIENT);
        if (!isMulti) {
            setWaitingNextTurn(pauseOpen);
            gameLoop.setPaused(pauseOpen);
        }
        // En multi, setPaused est de toute facon un no-op (cf GameLoop.setPaused),
        // mais on evite aussi setWaitingNextTurn pour ne pas bloquer les inputs.
    }

    private boolean scoreboardShowing = false;
    private KeyEventDispatcher tabDispatcher;

    private void installTabScoreboardDispatcher() {
        java.awt.KeyboardFocusManager fm = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager();
        tabDispatcher = e -> {
            if (e.getKeyCode() != KeyEvent.VK_TAB) return false;
            // n'agit que si la view du jeu est visible 
            if (gameBoardView == null || !gameBoardView.isShowing()) return false;
            // ignorer si un dialog est ouvert
            if (currentRoundEndPanel != null && currentRoundEndPanel.isShowing()) return false;

            if (e.getID() == KeyEvent.KEY_PRESSED) {
                showScoresOverlay();
                e.consume();
                return true;
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                hideScoresOverlay();
                e.consume();
                return true;
            }
            return false;
        };
        fm.addKeyEventDispatcher(tabDispatcher);
    }

    private void showScoresOverlay() {
        if (scoreboardShowing) return;
        scoreboardShowing = true;
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(
            gameManager.getPlayers(),
            gameManager.getRoundNumber()
        );
        gameBoardView.showScoreboardOverlay(panel);
    }

    private void hideScoresOverlay() {
        if (!scoreboardShowing) return;
        scoreboardShowing = false;
        gameBoardView.hideScoreboardOverlay();
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
            System.out.println("Elo et Money non appliqués car joueur DB introuvable");
            if (currentRoundEndPanel != null) {
                final RoundEndPanel panelRef = currentRoundEndPanel;
                SwingUtilities.invokeLater(() -> panelRef.showStatsError("Compte non connecté : pas de récompense"));
            }
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
        List<Integer> opponentElos = new ArrayList<>();
        for (PlayerModel opp : opponents) {
            if (opp instanceof BotModel) {
                opponentElos.add(0);
            } else {
                int e = (opp.getPlayerData() != null) ? opp.getPlayerData().getElo() : 0;
                opponentElos.add(e);
            }
        }

        final int oldElo = PlayerAccount.actuelAccount.getElo();
        final int oldXp = PlayerAccount.actuelAccount.getXp();
        final int oldMoney = PlayerAccount.actuelAccount.getMoney();
        final fr.upc.cr1a.view.RoundEndPanel panelRef = currentRoundEndPanel;

        String dbUuid = PlayerAccount.actuelAccount.getID();
        new Thread(() -> {
            PlayerData updated = SendAPI.applyGameResult(dbUuid, wonGame, opponentTypes, opponentElos);
            if (updated != null) {
                PlayerAccount.actuelAccount = updated;
                System.out.println("Fin de partie: " + (wonGame ? "WIN" : "PERDU"));
                final int newElo = updated.getElo();
                final int newXp = updated.getXp();
                final int newMoney = updated.getMoney();
                SwingUtilities.invokeLater(() -> {
                    if (panelRef != null) {
                        panelRef.updateStats(newElo, newXp, newMoney,newElo - oldElo, newXp - oldXp, newMoney - oldMoney);
                    }
                });
            } else {
                System.out.println("Fin de partie: echec applyGameResult");
                SwingUtilities.invokeLater(() -> {
                    if (panelRef != null) {
                        panelRef.showStatsError(
                            ApiErrorManager.getLastUserMessageOrDefault("Synchronisation des récompenses impossible")
                        );
                    }
                    ErrorDialog.showError(gameBoardModel.getFrame(),"Resultat de partie",ApiErrorManager.getLastUserMessageOrDefault("Fin de manche ,les récompenses n'ont pas pu etre synchronisées.")
                    );
                });
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

    public HumanTurnController getHumanTurnController() {
        return this.humanTurnController;
    }

    public HelpController getHelpController(){
        return this.helpController;
    }
}
