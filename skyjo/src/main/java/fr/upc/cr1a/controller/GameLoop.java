package fr.upc.cr1a.controller;

import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolServer;
import fr.upc.cr1a.model.BotModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GameSession;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.view.GameBoardView;
import javax.swing.Timer;

/**
 * Class orchéstrant le système de tour, il dépendra de 2 controllers(HumanTurn et BotTurn) et du gameBoard
 * 
 */
public class GameLoop {
    
    private static final int TURN_DURATION_SECONDS = 20; // Test de Timer ICI
    private boolean timerEnabled; 

    private Timer turnCountdownTimer;
    private int remainingSeconds;

    private boolean pendingContinueAfterPause = false;

    private static final int ACTION_DELAY_MS = 1200;

    private final GameManager gameManager;
    private final GameBoardView gameBoardView;

    private GameBoardController humanController;
    private GameBoardBotController botController;

    private boolean gameActive = true;

    private TimeoutTurnController timeoutController;

    private boolean paused = false;

    private boolean initRevealJustFinished = false;

    public GameLoop(GameManager gameManager, GameBoardView gameBoardView) {
        this.gameManager = gameManager;
        this.gameBoardView = gameBoardView;
    }

    public void setHumanController(GameBoardController humanController) {
        this.humanController = humanController;
    }

    public void setBotController(GameBoardBotController botController) {
        this.botController = botController;
    }

    public void startGameLoop() {
        initRevealJustFinished = true;
        startCurrentFlow();
    }

    public void proceedAfterAction() {
        if (!gameActive) return;
        stopTurnCountdown();
        if (humanController != null) {
            humanController.setWaitingNextTurn(true);
        }
        Timer timer = new Timer(ACTION_DELAY_MS, e -> {
            if (!gameActive) return;
            if (paused) {
                pendingContinueAfterPause = true;
                return;
            }
            pendingContinueAfterPause = false;
            if (humanController != null) {
                humanController.setWaitingNextTurn(false);
            }
            continueFlow();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void continueFlow() {
        if (paused) return;
        if (!gameActive) return;
        if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) {
            initRevealJustFinished = true;
            continueInitRevealFlow();
            return;
        }
        if (initRevealJustFinished) {
            initRevealJustFinished = false;
            gameManager.firstPlayer();
            PlayerModel firstPlayer = gameManager.getCurrentPlayer();
            gameBoardView.setMancheLabel(gameManager.getRoundNumber());
            gameBoardView.showPlayerTransition(firstPlayer, () -> startCurrentFlow());
            return;
        }
        finishNormalTurnFlow();
    }

    private void continueInitRevealFlow() {
        if (!gameActive) return;
        // Multi : on est passe au joueur suivant dans la phase INIT_REVEAL
        broadcastTurnChangedIfHost();
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerTransition(currentPlayer, () -> {
            if (!gameActive) return;

            if (humanController != null) {
                humanController.refreshGuide();
            }

            gameBoardView.showPlayerCards(currentPlayer);

            if (currentPlayer instanceof BotModel) {
                stopTurnCountdown();
                gameBoardView.setTurnTimerText("--");
                if (botController != null) {
                    botController.handleBotTurn();
                }
            } else {
                startTurnCountdown();
            }
        });
    }
    
    private void finishNormalTurnFlow() {
        if (!gameActive) return;
        
        //si pause ne pas changer de joueur maintenant
        if (paused) {
            pendingContinueAfterPause = true;
            return;
        }
        
        gameManager.nextPlayer();
        if (gameManager.getGameState() == GameManager.GameState.ROUND_ENDED
                || gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
            // Multi : prevenir les clients que la partie / manche est terminee
            if (gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
                broadcastGameEndedIfHost();
            } else {
                broadcastRoundEndedIfHost();
            }
            if (humanController != null) {
                humanController.showRoundEndScreenFromLoop();
            }
            return;
        }
        PlayerModel nextPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerTransition(nextPlayer, () -> startCurrentFlow());
    }

    public void startCurrentFlow() {
        if (paused) return;
        if (!gameActive) return;
        if (gameManager.getTurnPhase() != GameManager.TurnPhase.INIT_REVEAL) {
            gameManager.startTurn();
        }
        broadcastTurnChangedIfHost();

        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerCards(currentPlayer);
        boolean isClient = (GameSession.status == GameSession.STATUS.CLIENT);
        if (!isClient && !(currentPlayer instanceof BotModel) && gameManager.isAutoControlled(currentPlayer)) {
            stopTurnCountdown();
            gameBoardView.setTurnTimerText("--");
            Timer autoPlayDelay = new Timer(800, e -> {
                if (!gameActive || paused) return;
                if (timeoutController != null) {
                    timeoutController.handleTimeoutForCurrentPlayer();
                }
            });
            autoPlayDelay.setRepeats(false);
            autoPlayDelay.start();
            return;
        }
        if (humanController != null) {
            humanController.refreshGuide();
        }
        if (currentPlayer instanceof BotModel) {
            stopTurnCountdown();
            gameBoardView.setTurnTimerText("--");

            if (botController != null) {
                botController.handleBotTurn();
            }
        } else {
            startTurnCountdown();
        }
    }

    private void startTurnCountdown() {
        if (!timerEnabled) {
            gameBoardView.setTurnTimerText("--");
            return;
        }
        stopTurnCountdown();
        remainingSeconds = TURN_DURATION_SECONDS;
        gameBoardView.setTurnTimerText(""+remainingSeconds);
        turnCountdownTimer = new Timer(1000, e -> {
            if (!isGameActive()) return;
            remainingSeconds--;
            gameBoardView.setTurnTimerText(""+remainingSeconds);
            if (remainingSeconds <= 0) {
                turnCountdownTimer.stop();
                if (GameSession.status == GameSession.STATUS.CLIENT) return;
                handleTurnTimeout();
            }
        });
        turnCountdownTimer.start();
    }

    /**
     * Recommence le timer
     */
    public void restartCountdownForNetworkTurn() {
        if (!timerEnabled) return;
        if (!gameActive) return;
        startTurnCountdown();
    }

    private void stopTurnCountdown() {
        if (turnCountdownTimer != null && turnCountdownTimer.isRunning()) {
            turnCountdownTimer.stop();
        }
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void stopGame() {
        gameActive = false;
        stopTurnCountdown();
        gameBoardView.setTurnTimerText("--");
    }

    private void handleTurnTimeout() {
        if (!timerEnabled) return;
        if (paused) return;
        if (!isGameActive()) return;
        if (GameSession.status == GameSession.STATUS.CLIENT) return;
        stopTurnCountdown();
        if (humanController != null) {
            humanController.setWaitingNextTurn(true);
        }
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        if (currentPlayer instanceof BotModel) {
            if (botController != null) {
                botController.handleBotTurn();
            }
            return;
        }
        gameManager.registerAfkTurn(currentPlayer);
        boolean wasAuto = gameManager.isAutoControlled(currentPlayer);
        if (gameManager.getAfkCount(currentPlayer) >= 2) {
            gameManager.setAutoControlled(currentPlayer, true);
            // Si on vient juste de franchir le seuil cote HOST, prevenir tous les clients
            //le client concerné affichera "AFK : vous etes remplace par un bot"
            if (!wasAuto && GameSession.status == GameSession.STATUS.HOST) {
                broadcastAfkReplacedIfHost(currentPlayer.getPlayerData().getID());
            }
        }
        if(timeoutController != null) {
            timeoutController.handleTimeoutForCurrentPlayer();
        }
    }

    private void broadcastAfkReplacedIfHost(String playerId) {
        if (GameSession.status != GameSession.STATUS.HOST) return;
        try {
            fr.upc.cr1a.controller.network.Host h =
                gameManager.getGameBoardModel().getFrame().getLobbyRoomController().getHost();
            if (h == null) return;
            NetworkProtocolServer srv = h.getGameHost();
            if (srv == null) return;
            srv.broadcastAfkReplaced(playerId);
        } catch (Exception e) {
            
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            stopTurnCountdown();
            if (botController != null) {
                botController.stopAllBotActions();
            }
            return;
        }
        if (botController != null) {
            botController.resumeBotActions();
        }
        if (pendingContinueAfterPause) {
            pendingContinueAfterPause = false;
            if (humanController != null) {
                humanController.setWaitingNextTurn(false);
            }
            continueFlow();
            return;
        }
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        if (currentPlayer != null && !(currentPlayer instanceof BotModel) && !gameManager.isAutoControlled(currentPlayer)) {
            if (humanController != null) {
                humanController.setWaitingNextTurn(false);
            }
            startTurnCountdown();
        } else {
            gameBoardView.setTurnTimerText("--");
            //relancer le bot si c'est son tour
            if (currentPlayer instanceof BotModel && botController != null) {
                botController.handleBotTurn();
            }
        }
    }

    public void setTimeoutController(TimeoutTurnController timeoutController) {
        this.timeoutController = timeoutController;
    }

    public void setTimerEnabled(boolean enabled) {
        this.timerEnabled = enabled;
        if (!enabled) {
            stopTurnCountdown();
            gameBoardView.setTurnTimerText("--");
        } else {
            startTurnCountdown();
        }
    }

    /**
     * Broadcast TURN_CHANGED aux clients si on est host, avec le joueur et la phase courante.
     */
    private void broadcastTurnChangedIfHost() {
        if (GameSession.status != GameSession.STATUS.HOST) return;
        try {
            fr.upc.cr1a.controller.network.Host h =
                gameManager.getGameBoardModel().getFrame().getLobbyRoomController().getHost();
            if (h == null) return;
            NetworkProtocolServer srv = h.getGameHost();
            if (srv == null) return;
            PlayerModel cp = gameManager.getCurrentPlayer();
            if (cp == null) return;
            srv.broadcastTurnChanged(cp.getPlayerData().getID(), gameManager.getTurnPhase().name());
        } catch (Exception e) {}
    }

    private void broadcastRoundEndedIfHost() {
        if (GameSession.status != GameSession.STATUS.HOST) return;
        try {
            fr.upc.cr1a.controller.network.Host h =
                gameManager.getGameBoardModel().getFrame().getLobbyRoomController().getHost();
            if (h == null) return;
            NetworkProtocolServer srv = h.getGameHost();
            if (srv == null) return;
            // Inclure le declencheur de fin de manche pour que les clients puissent l'afficher
            PlayerModel lrp = gameManager.getLastRoundPlayer();
            String lrpId = (lrp != null) ? lrp.getPlayerData().getID() : null;
            srv.broadcastRoundEnded(lrpId);
        } catch (Exception e) {}
    }

    /**
     * Broadcast UPDATE_GAME_ENDED aux clients quand on est host
     */
    private void broadcastGameEndedIfHost() {
        if (GameSession.status != GameSession.STATUS.HOST) return;
        try {
            fr.upc.cr1a.controller.network.Host h =
                gameManager.getGameBoardModel().getFrame().getLobbyRoomController().getHost();
            if (h == null) return;
            NetworkProtocolServer srv = h.getGameHost();
            if (srv == null) return;
            // Inclure le declencheur de fin de manche pour le RoundEndPanel cote client
            PlayerModel lrp = gameManager.getLastRoundPlayer();
            String lrpId = (lrp != null) ? lrp.getPlayerData().getID() : null;
            srv.broadcastGameEnded(lrpId);
        } catch (Exception e) {}
    }
}