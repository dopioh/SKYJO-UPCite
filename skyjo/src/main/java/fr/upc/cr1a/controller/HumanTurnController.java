package fr.upc.cr1a.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
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

    // stuff cartes actions
    private enum ActionMode {
        NONE,
        SPY_ONE_COLUMN,
        REPLACE_CARD_FIRST,
        REPLACE_CARD_SECOND,
        TRADE_SELECT_OWN_CARD,
        TRADE_SELECT_TARGET_PLAYER,
        TRADE_SELECT_TARGET_CARD,
        ALL_PLAYERS_DROP_SELECT_TARGET,
        ALL_PLAYERS_DROP_SELECT_CARD
    }
    private ActionMode actionMode = ActionMode.NONE;
    private int replaceFirstRow = -1;
    private int replaceFirstCol = -1;
    private int tradeOwnRow = -1;
    private int tradeOwnCol = -1;
    private int tradeTargetPlayerIndex = -1;
    private final ArrayList<Integer> allPlayersDropPendingTargets = new ArrayList<>();
    private int allPlayersDropCurrentTargetIndex = -1;

    // fin du stuff actions

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

    /**
     * désactive les listeners si tous les joueurs sont des bots
     */
    public void disableListenersIfOnlyBots() {
        var players = gameManager.getPlayers();
        
        // verifier s'il y a au moins un joueur humain
        if (players != null && !players.isEmpty()) {
            boolean hasHumanPlayer = players.stream().anyMatch(p -> !p.getPlayerData().isBot());
            
            //si tous les joueurs sont des bots, désactiver les listeners
            if (!hasHumanPlayer) {
                removeAllListeners();
            }
        }
    }

    // pour selectionner un joueur cible
    public boolean handlePlayerButtonClicked(int targetIndex) {
        if (actionMode == ActionMode.TRADE_SELECT_TARGET_PLAYER || actionMode == ActionMode.TRADE_SELECT_TARGET_CARD) {
            selectTradeTargetPlayer(targetIndex);
            return true;
        }
        if (actionMode == ActionMode.ALL_PLAYERS_DROP_SELECT_TARGET || actionMode == ActionMode.ALL_PLAYERS_DROP_SELECT_CARD) {
            selectAllPlayersDropTarget(targetIndex);
            return true;
        }
        return false;
    }

    private void installDeckListeners() {
        //listener pour le deck de cartes actions
        gameBoardView.getDeckActionButton().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (mainController.isWaitingNextTurn()) return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION) return;

            PlayerModel player = gameManager.getCurrentPlayer();
            if (player == null) return;

            ArrayList<ActionType> actions = player.getActionHand();
            if (actions == null || actions.isEmpty()) return;

            //ouvrir l'overlay des cartes action
            gameBoardView.openActionCardOverlay(actions);

            //configue le retour quand une carte est selec
            gameBoardView.setActionCardSelectionCallback(() -> {
                int selectedIndex = gameBoardView.getSelectedActionCardIndex();
                if (selectedIndex >= 0 && selectedIndex < actions.size()) {
                    ActionType type = actions.get(selectedIndex);
                    onActionCardClicked(type, selectedIndex);
                }
            });
        });

        gameBoardView.getBtnPioche().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (mainController.isWaitingNextTurn()) return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION) return;

            CardModel drawn = gameManager.drawDeck();
            if (drawn.isFaceDown()) drawn.returnCard();

            String drawnPath = gameBoardView.getCardImagePath(drawn);
            gameBoardView.getBtnPioche().flip(() -> gameBoardView.getBtnPioche().setCardImage(drawnPath));
            gameBoardView.showCursorCard(drawnPath);
            gameBoardView.updateDiscardOnly(
                gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard()
            );

            PlayerModel player = gameManager.getCurrentPlayer();
            if (player != null && GameManager.isStarCard(drawn)) {
                grantActionForStar(player, "ETOILE : carte action gagnée automatiquement");
            }

            mainController.refreshGuide();
        });

        gameBoardView.getBtnDefausse().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (mainController.isWaitingNextTurn()) return;

            if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                if (gameManager.getDiscardPile().isEmpty()) return;

                CardModel taken = gameManager.takeDiscard();
                String takenPath = gameBoardView.getCardImagePath(taken);
                gameBoardView.getBtnDefausse().animatePressLight();
                gameBoardView.getBtnDefausse().setCardImage(takenPath);
                gameBoardView.showCursorCard(takenPath);
                mainController.updateAllDecksDisplay();
                mainController.refreshGuide();
                return;
            }

            if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
                gameManager.discardDrawCard();
                gameBoardView.getBtnDefausse().animatePressLight();
                waitingReveal = true;
                gameBoardView.hideCursorCard();
                mainController.updateAllDecksDisplay();
            }

            mainController.refreshGuide();
        });

        gameBoardView.getBtnPiocheAction().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (mainController.isWaitingNextTurn()) return;
            if (!gameManager.withActionCard()) return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION) return;

            PlayerModel player = gameManager.getCurrentPlayer();
            if (player == null) return;

            ActionCardModel actionCard = gameManager.drawActionRewardCard();
            if (actionCard == null) {
                gameBoardView.setActionGuide("Aucune carte action disponible.");
                return;
            }

            player.getActionHand().add(actionCard.getType());
            System.out.println("Carte action piochée : " + actionCard.getType());
            gameBoardView.showPlayerCards(player);
            mainController.updateAllDecksDisplay();

            gameManager.setTurnPhase(GameManager.TurnPhase.TURN_ENDED);
            mainController.refreshGuide();
            gameManager.resetAfkCount(gameManager.getCurrentPlayer());
            gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
            gameLoop.proceedAfterAction();
        });

        gameBoardView.getBtnDefausseAction().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!gameManager.withActionCard()) {
                    return;
                }
                if (gameManager.getDiscardActionPile() == null || gameManager.getDiscardActionPile().isEmpty()) {
                    return;
                }

                CardModel topCard = gameManager.getDiscardActionPile().getTopCard();
                if (topCard instanceof ActionCardModel actionCard) {
                    gameBoardView.showActionDescription(actionCard.getType());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                gameBoardView.hideActionDescription();
            }
        });

        //listener pour ouvrir l'overlay
        gameBoardView.getDeckActionButton().addActionListener(e -> {
            if (mainController.isWaitingNextTurn()) return;
            if (actionMode != ActionMode.NONE) return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION) return;

            PlayerModel player = gameManager.getCurrentPlayer();
            if (player == null) return;

            ArrayList<ActionType> actions = player.getActionHand();
            if (actions == null || actions.isEmpty()) return;

            //ouvrir l'overlay
            gameBoardView.openActionCardOverlay(actions);
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

                        CardModel revealedCard = revealedPlayer.getHand().getCard(r, c);
                        if (GameManager.isStarCard(revealedCard)) {
                            grantActionForStar(revealedPlayer, "ETOILE révélée : carte action gagnée");
                        }

                        gameBoardView.flipCardAt(r, c, () -> gameBoardView.showPlayerCards(revealedPlayer));
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
                        CardModel revealedCard = currentPlayer.getHand().getCard(r, c);
                        if (GameManager.isStarCard(revealedCard)) {
                            grantActionForStar(currentPlayer, "ETOILE révélée : carte action gagnée");
                        }
                        GameRuleModel.removeIdenticalColumns(currentPlayer.getHand(), gameManager.getDiscardPile(), currentPlayer);
                        GameRuleModel.removeIdenticalRows(currentPlayer.getHand(), gameManager.getDiscardPile(), currentPlayer);

                        //vérifier si fin de manche
                        gameManager.checkAndTriggerLastRound();

                        gameBoardView.flipCardAt(r, c, () -> gameBoardView.showPlayerCards(currentPlayer));

                        waitingReveal = false;
                        mainController.refreshGuide();
                        gameManager.resetAfkCount(gameManager.getCurrentPlayer());
                        gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
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
                        gameManager.resetAfkCount(gameManager.getCurrentPlayer());
                        gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
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
       
    }

    public void removeAllListeners() {
        //réinitialise tout les états
        waitingReveal = false;
        actionMode = ActionMode.NONE;
        replaceFirstRow = -1;
        replaceFirstCol = -1;
        tradeOwnRow = -1;
        tradeOwnCol = -1;
        tradeTargetPlayerIndex = -1;
        allPlayersDropPendingTargets.clear();
        allPlayersDropCurrentTargetIndex = -1;

        // Retire les listeners des boutons deck
        ActionListener[] piocheListeners = gameBoardView.getBtnPioche().getActionListeners();
        for (ActionListener listener : piocheListeners) {
            gameBoardView.getBtnPioche().removeActionListener(listener);
        }

        ActionListener[] defausseListeners = gameBoardView.getBtnDefausse().getActionListeners();
        for (ActionListener listener : defausseListeners) {
            gameBoardView.getBtnDefausse().removeActionListener(listener);
        }

        ActionListener[] piocheActionListeners = gameBoardView.getBtnPiocheAction().getActionListeners();
        for (ActionListener listener : piocheActionListeners) {
            gameBoardView.getBtnPiocheAction().removeActionListener(listener);
        }

        MouseListener[] defausseActionListeners = gameBoardView.getBtnDefausseAction().getMouseListeners();
        for (MouseListener listener : defausseActionListeners) {
            gameBoardView.getBtnDefausseAction().removeMouseListener(listener);
        }

        // Retire les listeners des cartes de main
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                var btn = gameBoardView.getCardButtons()[row][col];
                ActionListener[] cardListeners = btn.getActionListeners();
                for (ActionListener listener : cardListeners) {
                    btn.removeActionListener(listener);
                }
            }
        }

        // Retire les listeners des cartes action
        for (int i = 0; i < gameBoardView.getActionCardButtons().length; i++) {
            var btn = gameBoardView.getActionCardButtons()[i];
            if (btn != null) {
                MouseListener[] mouseListeners = btn.getMouseListeners();
                for (MouseListener listener : mouseListeners) {
                    btn.removeMouseListener(listener);
                }
            }
        }
        
        //retirer le retour du deck action button
        if (gameBoardView.getDeckActionButton() != null) {
            ActionListener[] deckListeners = gameBoardView.getDeckActionButton().getActionListeners();
            for (ActionListener listener : deckListeners) {
                gameBoardView.getDeckActionButton().removeActionListener(listener);
            }
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
                gameBoardView.setActionGuide("PLAY_TWO_TURN activé ! Tu joueras 2 tours.");
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

            case TRADE_CARD_WITH_PLAYER:
                gameManager.getDiscardActionPile().addCardAtTop(c);
                actionMode = ActionMode.TRADE_SELECT_OWN_CARD;
                resetTradeSelection();
                player.getActionHand().remove(indexInHand);
                gameBoardView.setActionHandVisible(true);
                gameBoardView.showOtherPlayerMain(false);
                gameBoardView.showPlayerCards(player);
                gameBoardView.setActionGuide("TRADE : Clique une de tes cartes à échanger");
                System.out.println("carte TRADE_CARD_WITH_PLAYER activée !");
                break;

            case ALL_PLAYERS_DROP_CARD:
                gameManager.getDiscardActionPile().addCardAtTop(c);
                player.getActionHand().remove(indexInHand);
                startAllPlayersDropAction(player);
                System.out.println("carte ALL_PLAYERS_DROP_CARD activée !");
                break;

            case PLAY_LAST_ACTION_IN_DEFAUSSE:
                player.getActionHand().remove(indexInHand);
                gameBoardView.showPlayerCards(player);

                ActionType replayedAction = getLastReplayableActionInDiscard();
                if (replayedAction == null) {
                    gameBoardView.setActionGuide("REPLAY : aucune action rejouable dans la défausse");
                    mainController.updateAllDecksDisplay();
                    System.out.println("PLAY_LAST_ACTION_IN_DEFAUSSE: aucune action rejouable");
                    break;
                }

                switch (replayedAction) {
                    case PLAY_TWO_TURN:
                        gameManager.grantExtraTurn();
                        gameBoardView.setActionGuide("REPLAY : PLAY_TWO_TURN activé ! Tu auras 1 tour supplémentaire.");
                        break;
                    case SPY_ONE_COLUMN:
                        actionMode = ActionMode.SPY_ONE_COLUMN;
                        gameBoardView.setActionGuide("REPLAY SPY : Clique sur une carte pour espionner sa colonne");
                        break;
                    case REPLACE_CARD_IN_HAND:
                        actionMode = ActionMode.REPLACE_CARD_FIRST;
                        replaceFirstRow = -1;
                        replaceFirstCol = -1;
                        gameBoardView.setActionGuide("REPLAY ECHANGE : Clique sur la 1ère carte à échanger");
                        break;
                    case TRADE_CARD_WITH_PLAYER:
                        actionMode = ActionMode.TRADE_SELECT_OWN_CARD;
                        resetTradeSelection();
                        gameBoardView.setActionHandVisible(true);
                        gameBoardView.showOtherPlayerMain(false);
                        gameBoardView.showPlayerCards(player);
                        gameBoardView.setActionGuide("REPLAY TRADE : Clique une de tes cartes à échanger");
                        break;
                    case ALL_PLAYERS_DROP_CARD:
                        startAllPlayersDropAction(player);
                        break;
                    default:
                        gameBoardView.setActionGuide("REPLAY : " + replayedAction + " non implémentée en graphique");
                        break;
                }
                mainController.updateAllDecksDisplay();
                System.out.println("carte PLAY_LAST_ACTION_IN_DEFAUSSE activée ! -> " + replayedAction);
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
                    gameManager.resetAfkCount(gameManager.getCurrentPlayer());
                    gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
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

                gameManager.resetAfkCount(gameManager.getCurrentPlayer());
                gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
                gameLoop.proceedAfterAction();
                break;

            case TRADE_SELECT_OWN_CARD:
                CardModel ownCard = player.getHand().getCard(row, col);
                if (ownCard == null) return;

                tradeOwnRow = row;
                tradeOwnCol = col;
                actionMode = ActionMode.TRADE_SELECT_TARGET_PLAYER;
                gameBoardView.setActionGuide("TRADE : Clique un joueur cible à gauche (modifiable)");
                break;

            case TRADE_SELECT_TARGET_PLAYER:
                gameBoardView.setActionGuide("TRADE : Choisis d'abord un joueur à gauche");
                break;

            case TRADE_SELECT_TARGET_CARD:
                completeTrade(row, col);
                break;

            case ALL_PLAYERS_DROP_SELECT_TARGET:
                gameBoardView.setActionGuide("DROP : Choisis un joueur à gauche");
                break;

            case ALL_PLAYERS_DROP_SELECT_CARD:
                applyAllPlayersDropOnSelectedCard(row, col);
                break;

            default:
                break;
        }
    }

    private ActionType getLastReplayableActionInDiscard() {
        if (gameManager.getDiscardActionPile() == null) return null;
        ArrayList<CardModel> stack = gameManager.getDiscardActionPile().getStack();
        for (int i = 0; i < stack.size(); i++) {
            CardModel card = stack.get(i);
            if (!(card instanceof ActionCardModel actionCard)) {
                continue;
            }

            ActionType type = actionCard.getType();
            if (type == ActionType.PLAY_LAST_ACTION_IN_DEFAUSSE) {
                continue;
            }
            return type;
        }
        return null;
    }

    private void selectTradeTargetPlayer(int targetIndex) {
        if (actionMode != ActionMode.TRADE_SELECT_TARGET_PLAYER && actionMode != ActionMode.TRADE_SELECT_TARGET_CARD) {
            return;
        }
        if (targetIndex == gameManager.getCurrentPlayerIndex()) {
            gameBoardView.setActionGuide("TRADE : choisis un autre joueur");
            return;
        }
        if (targetIndex < 0 || targetIndex >= gameManager.getPlayers().size()) {
            return;
        }

        tradeTargetPlayerIndex = targetIndex;
        PlayerModel target = gameManager.getPlayers().get(targetIndex);
        gameBoardView.showOtherPlayerMain(false);
        gameBoardView.setActionHandVisible(false);
        gameBoardView.showPlayerCards(target);
        actionMode = ActionMode.TRADE_SELECT_TARGET_CARD;
        gameBoardView.setActionGuide("TRADE : Clique une carte de " + target.getName() + " (tu peux changer de cible)");
    }

    private void completeTrade(int targetRow, int targetCol) {
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        if (currentPlayer == null) return;
        if (tradeTargetPlayerIndex < 0 || tradeOwnRow < 0 || tradeOwnCol < 0) return;
        if (tradeTargetPlayerIndex >= gameManager.getPlayers().size()) return;

        PlayerModel targetPlayer = gameManager.getPlayers().get(tradeTargetPlayerIndex);
        CardModel myCard = currentPlayer.getHand().getCard(tradeOwnRow, tradeOwnCol);
        CardModel targetCard = targetPlayer.getHand().getCard(targetRow, targetCol);
        if (myCard == null || targetCard == null) {
            gameBoardView.setActionGuide("TRADE : case vide");
            return;
        }

        currentPlayer.getHand().replaceCard(tradeOwnRow, tradeOwnCol, targetCard);
        targetPlayer.getHand().replaceCard(targetRow, targetCol, myCard);

        GameRuleModel.removeIdenticalColumns(currentPlayer.getHand(), gameManager.getDiscardPile(), currentPlayer);
        GameRuleModel.removeIdenticalRows(currentPlayer.getHand(), gameManager.getDiscardPile(), currentPlayer);
        GameRuleModel.removeIdenticalColumns(targetPlayer.getHand(), gameManager.getDiscardPile(), targetPlayer);
        GameRuleModel.removeIdenticalRows(targetPlayer.getHand(), gameManager.getDiscardPile(), targetPlayer);

        //vérifier si fin de manche
        gameManager.checkAndTriggerLastRound();

        actionMode = ActionMode.NONE;
        resetTradeSelection();
        gameBoardView.setActionHandVisible(true);
        gameBoardView.showOtherPlayerMain(false);
        gameBoardView.showPlayerCards(currentPlayer);
        mainController.updateAllDecksDisplay();
        gameBoardView.setActionGuide("TRADE terminé !");
        gameManager.resetAfkCount(gameManager.getCurrentPlayer());
        gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
        gameLoop.proceedAfterAction();
    }

    private void startAllPlayersDropAction(PlayerModel currentPlayer) {
        resetAllPlayersDropSelection();
        for (int i = 0; i < gameManager.getPlayers().size(); i++) {
            if (i != gameManager.getCurrentPlayerIndex()) {
                allPlayersDropPendingTargets.add(i);
            }
        }

        if (allPlayersDropPendingTargets.isEmpty()) {
            actionMode = ActionMode.NONE;
            gameBoardView.showPlayerCards(currentPlayer);
            gameBoardView.setActionGuide("DROP : aucun joueur");
            gameManager.resetAfkCount(gameManager.getCurrentPlayer());
            gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
            gameLoop.proceedAfterAction();
            return;
        }

        actionMode = ActionMode.ALL_PLAYERS_DROP_SELECT_TARGET;
        gameBoardView.setActionHandVisible(false);
        gameBoardView.showOtherPlayerMain(false);
        gameBoardView.showPlayerCards(currentPlayer);
        gameBoardView.setActionGuide("DROP : Choisis un joueur à gauche (" + allPlayersDropPendingTargets.size() + " restants)");
    }

    private void selectAllPlayersDropTarget(int targetIndex) {
        if (actionMode != ActionMode.ALL_PLAYERS_DROP_SELECT_TARGET && actionMode != ActionMode.ALL_PLAYERS_DROP_SELECT_CARD) {
            return;
        }
        if (targetIndex == gameManager.getCurrentPlayerIndex()) {
            gameBoardView.setActionGuide("DROP : choisis un autre joueur");
            return;
        }
        if (!allPlayersDropPendingTargets.contains(targetIndex)) {
            gameBoardView.setActionGuide("DROP : ce joueur a déjà été affecté");
            return;
        }

        allPlayersDropCurrentTargetIndex = targetIndex;
        PlayerModel target = gameManager.getPlayers().get(targetIndex);
        gameBoardView.showOtherPlayerMain(false);
        gameBoardView.setActionHandVisible(false);
        gameBoardView.showPlayerCards(target);
        actionMode = ActionMode.ALL_PLAYERS_DROP_SELECT_CARD;
        gameBoardView.setActionGuide("DROP : Clique une carte de " + target.getName() + " (" + allPlayersDropPendingTargets.size() + " restants)");
    }

    private void applyAllPlayersDropOnSelectedCard(int row, int col) {
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        if (currentPlayer == null) return;
        if (allPlayersDropCurrentTargetIndex < 0) {
            return;
        }
        if (!allPlayersDropPendingTargets.contains(allPlayersDropCurrentTargetIndex)) {
            return;
        }

        PlayerModel targetPlayer = gameManager.getPlayers().get(allPlayersDropCurrentTargetIndex);
        CardModel targetCard = targetPlayer.getHand().getCard(row, col);
        if (targetCard == null) {
            gameBoardView.setActionGuide("DROP : case vide");
            return;
        }

        CardModel drawnCard = gameManager.drawDeck();
        if (drawnCard == null) {
            gameBoardView.setActionGuide("DROP : impossible");
            return;
        }
        if (drawnCard.isFaceDown()) {
            drawnCard.returnCard();
        }
        if (targetCard.isFaceDown()) {
            targetCard.returnCard();
        }

        targetPlayer.getHand().replaceCard(row, col, drawnCard);
        gameManager.getDiscardPile().addCardAtTop(targetCard);
        if (GameManager.isStarCard(drawnCard)) {
            grantActionForStar(targetPlayer, "ETOILE piochée via DROP : carte action gagnée");
        }

        GameRuleModel.removeIdenticalColumns(targetPlayer.getHand(), gameManager.getDiscardPile(), targetPlayer);
        GameRuleModel.removeIdenticalRows(targetPlayer.getHand(), gameManager.getDiscardPile(), targetPlayer);

        //vérifier si fin de manche
        gameManager.checkAndTriggerLastRound();

        allPlayersDropPendingTargets.remove(Integer.valueOf(allPlayersDropCurrentTargetIndex));
        allPlayersDropCurrentTargetIndex = -1;
        mainController.updateAllDecksDisplay();

        if (allPlayersDropPendingTargets.isEmpty()) {
            actionMode = ActionMode.NONE;
            resetAllPlayersDropSelection();
            gameBoardView.setActionHandVisible(true);
            gameBoardView.showOtherPlayerMain(false);
            gameBoardView.showPlayerCards(currentPlayer);
            gameBoardView.setActionGuide("DROP : tous les joueurs ont été affectés");
            gameManager.resetAfkCount(gameManager.getCurrentPlayer());
            gameManager.setAutoControlled(gameManager.getCurrentPlayer(), false);
            gameLoop.proceedAfterAction();
            return;
        }

        actionMode = ActionMode.ALL_PLAYERS_DROP_SELECT_TARGET;
        gameBoardView.showPlayerCards(currentPlayer);
        gameBoardView.setActionGuide("DROP : Choisis le prochain joueur (" + allPlayersDropPendingTargets.size() + " restants)");
    }

    private void grantActionForStar(PlayerModel player, String guide) {
        ActionCardModel actionCard = gameManager.grantActionForStar(player);
        if (actionCard == null) {
            return;
        }
        gameBoardView.showPlayerCards(player);
        mainController.updateAllDecksDisplay();
        gameBoardView.setActionGuide(guide);
    }

    private void resetTradeSelection() {
        tradeOwnRow = -1;
        tradeOwnCol = -1;
        tradeTargetPlayerIndex = -1;
    }

    private void resetAllPlayersDropSelection() {
        allPlayersDropPendingTargets.clear();
        allPlayersDropCurrentTargetIndex = -1;
    }

    public void resetTurnState() {
        waitingReveal = false;
        actionMode = ActionMode.NONE;
        replaceFirstRow = -1;
        replaceFirstCol = -1;
        resetTradeSelection();
        resetAllPlayersDropSelection();
        gameBoardView.setActionHandVisible(true);
        gameBoardView.showOtherPlayerMain(false);
        gameBoardView.hideActionDescription();
    }
}
