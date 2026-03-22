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

    private static final int ACTION_DELAY_MS = 1200;

    private final GameManager gameManager;
    private final GameBoardView gameBoardView;

    private GameBoardController humanController;
    private GameBoardBotController botController;

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
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerTransition(currentPlayer, () -> {
            if (humanController != null) {
                humanController.refreshGuide();
            }
            gameBoardView.showPlayerCards(currentPlayer);
            if (currentPlayer instanceof BotModel) {
                if (botController != null) {
                    botController.handleBotTurn();
                }
            }
        });
    }
    
    private void finishNormalTurnFlow() {
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
        if (gameManager.getTurnPhase() != GameManager.TurnPhase.INIT_REVEAL) {
            gameManager.startTurn();
        }
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        gameBoardView.showPlayerCards(currentPlayer);
        if (humanController != null) {
            humanController.refreshGuide();
        }
        if (currentPlayer instanceof BotModel) {
            if (botController != null) {
                botController.handleBotTurn();
            }
        }
    }
}