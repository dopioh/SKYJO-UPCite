package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.BotModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.view.GameBoardView;
import javax.swing.Timer;

/**
 * Class orchéstrant le système de tour, il dépendra de 2 controllers(HumanTurn et BotTurn) et du gameBoard
 * 
 */
public class GameLoop {
    
    private static final int TURN_DURATION_SECONDS = 45;

    private Timer turnCountdownTimer;
    private int remainingSeconds;

    private static final int ACTION_DELAY_MS = 1200;

    private final GameManager gameManager;
    private final GameBoardView gameBoardView;

    private GameBoardController humanController;
    private GameBoardBotController botController;

    private boolean gameActive = true;

    private TimeoutTurnController timeoutController;

    public void setTimeoutController(TimeoutTurnController timeoutController) {
        this.timeoutController = timeoutController;
    }
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
            if (humanController != null) {
                humanController.setWaitingNextTurn(false);
            }
            continueFlow();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void continueFlow() {
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
            gameBoardView.showPlayerTransition(firstPlayer, () -> startCurrentFlow());
            return;
        }
        finishNormalTurnFlow();
    }

    private void continueInitRevealFlow() {
        if (!gameActive) return;
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
        gameManager.nextPlayer();
        if (gameManager.getGameState() == GameManager.GameState.ROUND_ENDED
                || gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
            if (humanController != null) {
                humanController.showRoundEndScreenFromLoop();
            }
            return;
        }
        PlayerModel nextPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerTransition(nextPlayer, () -> startCurrentFlow());
    }

    public void startCurrentFlow() {
        if (!gameActive) return;
        if (gameManager.getTurnPhase() != GameManager.TurnPhase.INIT_REVEAL) {
            gameManager.startTurn();
        }
        
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerCards(currentPlayer);

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
        stopTurnCountdown();
        remainingSeconds = TURN_DURATION_SECONDS;
        gameBoardView.setTurnTimerText(""+remainingSeconds);
        turnCountdownTimer = new Timer(1000, e -> {
            if (!isGameActive()) return;
            remainingSeconds--;
            gameBoardView.setTurnTimerText(""+remainingSeconds);
            if (remainingSeconds <= 0) {
                turnCountdownTimer.stop();
                handleTurnTimeout();
            }
        });
        turnCountdownTimer.start();
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
        if (!isGameActive()) return;
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
        if (timeoutController != null) {
            timeoutController.handleTimeoutForCurrentPlayer();
        }
    }
}