package fr.upc.cr1a.controller;

import java.util.ArrayList;
import javax.swing.Timer;

import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GameRuleModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.view.GameBoardView;

/**
 * Classe servant a différencier toutes les actions joueurs
 */
public class HumanTurnController {

    private final GameBoardView gameBoardView;
    private final GameManager gameManager;
    private final GameBoardController mainController;
    private final GameLoop gameLoop;

    private boolean waitingReveal = false;

    private enum ActionMode { NONE, SPY_ONE_COLUMN, REPLACE_CARD_FIRST, REPLACE_CARD_SECOND }
    private ActionMode actionMode = ActionMode.NONE;
    private int replaceFirstRow = -1;
    private int replaceFirstCol = -1;

    public HumanTurnController(GameBoardView gameBoardView,GameManager gameManager,GameBoardController mainController,GameLoop gameLoop) {
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.mainController = mainController;
        this.gameLoop = gameLoop;
    }

    public void installListeners() {
        installDeckListeners();
        installHandListeners();
        installActionCardListeners();
    }

    private void installDeckListeners() {
        gameBoardView.getBtnPioche().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (mainController.isWaitingNextTurn()) return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION) return;

            CardModel drawn = gameManager.drawDeck();
            if (drawn.isFaceDown()) drawn.returnCard();

            String drawnPath = gameBoardView.getCardImagePath(drawn);
            gameBoardView.getBtnPioche().setCardImage(drawnPath);
            gameBoardView.showCursorCard(drawnPath);
            gameBoardView.updateDiscardOnly(
                gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard()
            );
            mainController.refreshGuide();
        });

        gameBoardView.getBtnDefausse().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (mainController.isWaitingNextTurn()) return;

            if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                if (gameManager.getDiscardPile().isEmpty()) return;

                CardModel taken = gameManager.takeDiscard();
                String takenPath = gameBoardView.getCardImagePath(taken);
                gameBoardView.getBtnDefausse().setCardImage(takenPath);
                gameBoardView.showCursorCard(takenPath);
                mainController.updateAllDecksDisplay();
                mainController.refreshGuide();
                return;
            }

            if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
                gameManager.discardDrawCard();
                waitingReveal = true;
                gameBoardView.hideCursorCard();
                mainController.updateAllDecksDisplay();
            }

            mainController.refreshGuide();
        });

        gameBoardView.getBtnPiocheAction().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (mainController.isWaitingNextTurn()) return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION) return;
            if (!gameManager.withActionCard()) return;

            PlayerModel player = gameManager.getCurrentPlayer();
            if (player.getStarsScore() <= 0) {
                System.out.println("Pas assez d'étoiles pour piocher une action !");
                return;
            }

            CardModel drawn = gameManager.drawActionDeck();
            if (drawn.isFaceDown()) drawn.returnCard();

            if (drawn instanceof ActionCardModel actionCard) {
                player.getActionHand().add(actionCard.getType());
                System.out.println("Carte action activée : " + actionCard.getType());
            }

            player.subStarsScore();

            gameBoardView.showPlayerCards(player);
            mainController.updateAllDecksDisplay();

            gameManager.setTurnPhase(GameManager.TurnPhase.TURN_ENDED);
            mainController.refreshGuide();
            gameLoop.proceedAfterAction();
        });
    }

    private void installHandListeners() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                final int r = row;
                final int c = col;

                gameBoardView.getCardButtons()[row][col].addActionListener(e -> {
                    if (mainController.isWaitingNextTurn()) return;

                    if (actionMode != ActionMode.NONE) {
                        handleActionModeClick(r, c);
                        return;
                    }

                    PlayerModel currentPlayer = gameBoardView.getCurrentPlayer();
                    if (currentPlayer == null) return;

                    CardModel card = currentPlayer.getHand().getCard(r, c);

                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) {
                        PlayerModel revealedPlayer = gameBoardView.getCurrentPlayer();
                        boolean ok = gameManager.revealInitCard(c, r);
                        if (!ok) return;

                        gameBoardView.showPlayerCards(revealedPlayer);
                        mainController.updateAllDecksDisplay();
                        mainController.refreshGuide();

                        //Première carte du reveal
                        if (gameManager.getInitRevealCount() == 1) {
                            return;
                        }
                        gameLoop.proceedAfterAction();
                        return;
                    }

                    if (waitingReveal) {
                        if (card == null || !card.isFaceDown()) return;

                        currentPlayer.getHand().revealCard(r, c);
                        GameRuleModel.removeIdenticalColumns(currentPlayer.getHand(), gameManager.getDiscardPile());
                        GameRuleModel.removeIdenticalRows(currentPlayer.getHand(), gameManager.getDiscardPile());

                        gameBoardView.showPlayerCards(currentPlayer);

                        waitingReveal = false;
                        gameManager.setTurnPhase(GameManager.TurnPhase.TURN_ENDED);
                        mainController.refreshGuide();
                        gameLoop.proceedAfterAction();
                        return;
                    }

                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK
                            || gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD) {

                        gameManager.exchangeCardInHand(c, r);
                        gameBoardView.hideCursorCard();
                        gameBoardView.showPlayerCards(currentPlayer);

                        mainController.updateAllDecksDisplay();
                        mainController.refreshGuide();
                        gameLoop.proceedAfterAction();
                        return;
                    }

                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                        mainController.refreshGuide();
                        return;
                    }

                    mainController.refreshGuide();
                });
            }
        }
    }

    private void installActionCardListeners() {
        for (int i = 0; i < gameBoardView.getActionCardButtons().length; i++) {
            final int id = i;
            gameBoardView.getActionCardButtons()[i].addActionListener(e -> {
                if (mainController.isWaitingNextTurn()) return;
                if (actionMode != ActionMode.NONE) return;
                if (id >= gameBoardView.getVisibleActionCount()) return;

                ActionType type = gameBoardView.getActionCardType(id);
                if (type == null) return;

                onActionCardClicked(type, id);
            });
        }
    }

    private void onActionCardClicked(ActionType type, int indexInHand) {
        if (mainController.isWaitingNextTurn()) return;
        if (actionMode != ActionMode.NONE) return;

        PlayerModel player = gameManager.getCurrentPlayer();
        if (player == null) return;

        ActionCardModel c = new ActionCardModel(false, type);

        switch (type) {
            case PLAY_TWO_TURN:
                gameManager.getDiscardActionPile().addCardAtTop(c);
                gameManager.grantExtraTurn();
                player.getActionHand().remove(indexInHand);
                gameBoardView.showPlayerCards(player);
                gameBoardView.setActionGuide("PLAY_TWO_TURN activé ! Tu rejoues 2 fois.");
                System.out.println("carte PLAY_TWO_TURN activée !");
                break;

            case SPY_ONE_COLUMN:
                gameManager.getDiscardActionPile().addCardAtTop(c);
                actionMode = ActionMode.SPY_ONE_COLUMN;
                player.getActionHand().remove(indexInHand);
                gameBoardView.showPlayerCards(player);
                gameBoardView.setActionGuide("SPY : Clique sur une carte pour espionner sa colonne");
                System.out.println("carte SPY_ONE_COLUMN activée !");
                break;

            case REPLACE_CARD_IN_HAND:
                gameManager.getDiscardActionPile().addCardAtTop(c);
                actionMode = ActionMode.REPLACE_CARD_FIRST;
                replaceFirstRow = -1;
                replaceFirstCol = -1;
                player.getActionHand().remove(indexInHand);
                gameBoardView.showPlayerCards(player);
                gameBoardView.setActionGuide("SWAP : Clique sur la 1ère carte à échanger");
                System.out.println("carte REPLACE_CARD_IN_HAND activée !");
                break;

            default:
                System.out.println("carte " + type + " activée ! (pas encore implémentée en graphique)");
                break;
        }
    }

    private void handleActionModeClick(int row, int col) {
        PlayerModel player = gameManager.getCurrentPlayer();
        if (player == null) return;

        switch (actionMode) {
            case SPY_ONE_COLUMN:
                PlayerHandModel hand = player.getHand();
                if (hand == null) return;

                ArrayList<int[]> hiddenCards = new ArrayList<>();

                for (int r = 0; r < 3; r++) {
                    CardModel card = hand.getCard(r, col);
                    if (card != null && card.isFaceDown()) {
                        card.returnCard();
                        hiddenCards.add(new int[] { r, col });
                    }
                }

                gameBoardView.showPlayerCards(player);
                gameBoardView.setActionGuide("Colonne " + col + " révélée ! (3 secondes...)");
                actionMode = ActionMode.NONE;

                Timer hideTimer = new Timer(3000, ev -> {
                    for (int[] pos : hiddenCards) {
                        CardModel card = hand.getCard(pos[0], pos[1]);
                        if (card != null && !card.isFaceDown()) {
                            card.returnCard();
                        }
                    }
                    gameBoardView.showPlayerCards(player);
                    mainController.refreshGuide();
                    gameLoop.proceedAfterAction();
                });
                hideTimer.setRepeats(false);
                hideTimer.start();
                break;

            case REPLACE_CARD_FIRST:
                CardModel card = player.getHand().getCard(row, col);
                if (card == null) return;

                replaceFirstRow = row;
                replaceFirstCol = col;
                actionMode = ActionMode.REPLACE_CARD_SECOND;
                gameBoardView.setActionGuide("SWAP : Clique sur la 2ème carte à échanger");
                break;

            case REPLACE_CARD_SECOND:
                if (row == replaceFirstRow && col == replaceFirstCol) return;

                CardModel firstCard = player.getHand().getCard(replaceFirstRow, replaceFirstCol);
                CardModel secondCard = player.getHand().getCard(row, col);
                if (firstCard == null || secondCard == null) return;

                player.getHand().replaceCard(replaceFirstRow, replaceFirstCol, secondCard);
                player.getHand().replaceCard(row, col, firstCard);

                gameBoardView.showPlayerCards(player);
                gameBoardView.setActionGuide("Cartes échangées !");
                System.out.println("REPLACE_CARD_IN_HAND: échange [" + replaceFirstRow + "," + replaceFirstCol + "] <-> [" + row + "," + col + "]");

                actionMode = ActionMode.NONE;
                replaceFirstRow = -1;
                replaceFirstCol = -1;

                gameLoop.proceedAfterAction();
                break;

            default:
                break;
        }
    }

    public void resetTurnState() {
        waitingReveal = false;
        actionMode = ActionMode.NONE;
        replaceFirstRow = -1;
        replaceFirstCol = -1;
    }
}