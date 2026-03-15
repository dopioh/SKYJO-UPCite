package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.BotModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.view.GameBoardView;

/**
 * Class orchéstrant le système de tour, il dépendra de 2 controllers(HumanTurn et BotTurn) et du gameBoard
 * 
 */
public class GameLoop {

    private final GameManager gameManager;
    private final GameBoardView gameBoardView;

    private GameBoardController humanController;
    private GameBoardBotController botController;

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
        startCurrentTurn();
    }

    public void startCurrentTurn() {
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

    public void requestEndTurn() {
        finishCurrentTurn();
    }

    private void finishCurrentTurn() {
        gameManager.nextPlayer();
        if (gameManager.getGameState() == GameManager.GameState.ROUND_ENDED || gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
            if (humanController != null) {
                humanController.showRoundEndScreenFromLoop();
            }
            return;
        }
        startCurrentTurn();
    }
}