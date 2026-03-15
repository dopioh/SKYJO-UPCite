package fr.upc.cr1a.controller;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import fr.upc.cr1a.model.*;
import fr.upc.cr1a.model.GameManager.TurnPhase;
import fr.upc.cr1a.view.*;

import fr.upc.cr1a.model.ActionCardModel.ActionType;

/**
 * Controller pour gerer les connections entre la view et le model
 * Du plateau de jeu
 * 
 * @see GameBoardView vue graphique
 * @see GameBoardModel logique du plateau
 * 
 * @author Mei,Ian,Hugo,Billy,Nassim
 */
public class GameBoardController {

    GameBoardModel gameBoardModel;
    GameBoardView gameBoardView;
    NextRoundPanel nextRoundPanel;
    GameManager gameManager;
    private GameBoardBotController botController;
    
    private boolean waitingNextTurn = false; // empeche de cliquer pendant l'attente
    private boolean waitingReveal = false;
    // pour fixer le clique du playerpreview
    private boolean isPinned = false;
    private AudioManager audio;
    // panel de fin de manche
    RoundEndPanel currentRoundEndPanel = null;

    // stuff pour les actions 
    // ActionMode ressemble a ActionType SAUF QUE ActionType marchait pour le jeu en terminal
    // j'ai besoin d'autres états pour la logique des actions graphiques
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

    /**
     * Initialise la view et le Model du GameBoard
     */
    public GameBoardController(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameManager gameManager) {
        this.gameBoardModel = gameBoardModel;
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.audio = new AudioManager();
        this.nextRoundPanel = gameBoardView.getNextRoundPanel();
        this.botController = new GameBoardBotController(gameBoardView, gameManager, this);

        listener();
        refreshGuide();
        
        gameBoardView.setActionDecksVisible(gameManager.withActionCard());
        
        // Initialiser l'affichage des pioches (normales + actions)
        updateAllDecksDisplay();
        
        //vérifie si c'est un bot
        SwingUtilities.invokeLater(() -> botController.handleBotTurn());
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
        retour.addActionListener(e -> gameBoardModel.retourMenuCalled());

        gameBoardView.getBtnPioche().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL)
                return;
            if (waitingNextTurn)
                return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION)
                return;

            CardModel drawn = gameManager.drawDeck();
            if (drawn.isFaceDown()) drawn.returnCard();
            String drawnPath = gameBoardView.getCardImagePath(drawn);
            gameBoardView.getBtnPioche().setCardImage(drawnPath);
            gameBoardView.showCursorCard(drawnPath);
            gameBoardView.updateDiscardOnly(
                    gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard());
            refreshGuide();
        });

        gameBoardView.getBtnDefausse().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL)
                return;
            if (waitingNextTurn)
                return;
            // Si on prend a partir de la défausse
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                if (gameManager.getDiscardPile().isEmpty())
                    return;
                CardModel taken = gameManager.takeDiscard();
                String takenPath = gameBoardView.getCardImagePath(taken);
                gameBoardView.getBtnDefausse().setCardImage(takenPath);
                gameBoardView.showCursorCard(takenPath);
                updateAllDecksDisplay();
                refreshGuide();
                return;
            }

            // Reveal de la carte du deck
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
                gameManager.discardDrawCard();
                waitingReveal = true; // maintenant il doit révéler une carte
                gameBoardView.hideCursorCard();

                updateAllDecksDisplay();
            }
            refreshGuide();
        });

        gameBoardView.getBtnPiocheAction().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (waitingNextTurn) return;
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
            updateAllDecksDisplay();

            gameManager.setTurnPhase(GameManager.TurnPhase.TURN_ENDED);
            refreshGuide();
            switchToNextPlayer();
        });

        // listeners pour les cartes, retourner la carte au clic puis switch joueur
        // après 2s
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                final int r = row;
                final int c = col;
                gameBoardView.getCardButtons()[row][col].addActionListener(e -> {
                    if (waitingNextTurn)
                        return; // bloque les clics pendant l'attente

                    // si il y'a une action en cours on la fait au lieu de faire le comportement habituelle
                    if (actionMode != ActionMode.NONE) {
                        handleActionModeClick(r, c);
                        return;
                    }

                    PlayerModel currentPlayer = gameBoardView.getCurrentPlayer();
                    if (currentPlayer == null)
                        return;

                    CardModel card = currentPlayer.getHand().getCard(r, c);

                    // Partie Reveal de 2 cartes en debut de partie
                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) { // que pendant le choix des
                                                                                           // deux prems cartes
                        boolean ok = gameManager.revealInitCard(c, r);
                        if (!ok)
                            return;
                        gameBoardView.showPlayerCards(gameManager.getCurrentPlayer());
                        updateAllDecksDisplay();
                        refreshGuide();
                        //System.out.println("ALROS ??");
                        
                        //vérifie si après le tour d'un bot c'est un joeur qui doit jouer pour afficher sa main
                        Timer checkBotTimer = new Timer(300, ev -> {
                            botController.handleBotTurn();
                        });
                        checkBotTimer.setRepeats(false);
                        checkBotTimer.start();
                        //System.out.println("ALROS ??");
                        return;
                    }
                    //System.out.println("ALROS ??");

                    // Reveler apres avoir défausser
                    if (waitingReveal) {
                        if (card == null || !card.isFaceDown())
                            return;

                        currentPlayer.getHand().revealCard(r, c);
                        
                        //regarde et supprimer les colonnes/lignes identiques
                        GameRuleModel.removeIdenticalColumns(currentPlayer.getHand(), gameManager.getDiscardPile());
                        GameRuleModel.removeIdenticalRows(currentPlayer.getHand(), gameManager.getDiscardPile());
                        
                        gameBoardView.showPlayerCards(currentPlayer);

                        waitingReveal = false;
                        refreshGuide();
                        switchToNextPlayer();
                        gameManager.setTurnPhase(TurnPhase.TURN_ENDED);
                        refreshGuide();
                        return;
                    }

                    // Echange de carte pioche deck
                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK
                            || gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD) {

                        gameManager.exchangeCardInHand(c, r); // col, row (comme ton code)
                        gameBoardView.hideCursorCard();
                        gameBoardView.showPlayerCards(currentPlayer);

                        updateAllDecksDisplay();

                        refreshGuide();
                        switchToNextPlayer();
                        return;
                    }

                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                        refreshGuide();
                        return;
                    }
                    refreshGuide();
                    return;
                });
            }
        }

        // listeners cartes action
        for (int i = 0; i < gameBoardView.getActionCardButtons().length; i++) {
            final int id = i;
            gameBoardView.getActionCardButtons()[i].addActionListener(e -> {
                if (waitingNextTurn) return;
                if (actionMode != ActionMode.NONE) return;
                if (id >= gameBoardView.getVisibleActionCount()) return;
                ActionType type = gameBoardView.getActionCardType(id);
                if (type == null) return;
                onActionCardClicked(type, id);
            });
        }

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
                    if (actionMode == ActionMode.TRADE_SELECT_TARGET_PLAYER || actionMode == ActionMode.TRADE_SELECT_TARGET_CARD) {
                        selectTradeTargetPlayer(tmp);
                        return;
                    }
                    if (actionMode == ActionMode.ALL_PLAYERS_DROP_SELECT_TARGET || actionMode == ActionMode.ALL_PLAYERS_DROP_SELECT_CARD) {
                        selectAllPlayersDropTarget(tmp);
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
     * Logique d'activation d'une carte action quand on clique dessus
     */
    private void onActionCardClicked(ActionType type, int indexInHand) {
        if (waitingNextTurn) return;
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
                gameBoardView.setActionGuide("PLAY_TWO_TURN activé ! Tu auras 1 tour supplémentaire.");
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
                gameBoardView.setActionGuide("ECHANGE : Clique sur la 1ère carte à échanger");
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
                    updateAllDecksDisplay();
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
                updateAllDecksDisplay();
                System.out.println("carte PLAY_LAST_ACTION_IN_DEFAUSSE activée ! -> " + replayedAction);
                break;

            default:
                System.out.println("carte " + type + " activée ! (pas encore implémentée en graphique)");
                break;
        }
    }

    private ActionType getLastReplayableActionInDiscard() {
        DefausseActionModel discardActionPile = gameManager.getDiscardActionPile();
        if (discardActionPile == null) return null;

        ArrayList<CardModel> stack = discardActionPile.getStack();
        for (int i = 0; i < stack.size(); i++) {
            CardModel card = stack.get(i);
            if (!(card instanceof ActionCardModel actionCard)) continue;

            ActionType type = actionCard.getType();
            if (type == ActionType.DEFENSE) continue;
            return type;
        }
        return null;
    }

    /**
     * Logique de la carte action une fois activée
     * Lorsqu'on choisit une carte ou un joueur sur le gameboard
     */
    private void handleActionModeClick(int row, int col) {
        switch (actionMode) {
            case SPY_ONE_COLUMN:
                PlayerModel player = gameManager.getCurrentPlayer();
                if (player == null) return;
                PlayerHandModel hand = player.getHand();
                if (hand == null) return;
                ArrayList<int[]> hiddenCards = new ArrayList<>();

                for (int r = 0; r < 3; r++) {
                    CardModel card = hand.getCard(r, col);
                    if (card != null && card.isFaceDown()) {
                        card.returnCard();
                        hiddenCards.add(new int[]{r, col});
                    }
                }
                gameBoardView.showPlayerCards(player);
                gameBoardView.setActionGuide("Colonne " + col + " révélée ! (3 secondes...)");
                actionMode = ActionMode.NONE;

                // on laisse 3s et on retourne les cartes qui n'étaient pas deja retournées
                Timer hideTimer = new Timer(3000, ev -> {
                    for (int[] pos : hiddenCards) {
                        CardModel card = hand.getCard(pos[0], pos[1]);
                        if (card != null && !card.isFaceDown()) {
                            card.returnCard();
                        }
                    }
                    gameBoardView.showPlayerCards(player);
                    refreshGuide();
                    switchToNextPlayer();
                });
                hideTimer.setRepeats(false);
                hideTimer.start();
                break;
            
            case REPLACE_CARD_FIRST:
                player = gameManager.getCurrentPlayer();
                if (player == null) return;
                CardModel card = player.getHand().getCard(row, col);
                if (card == null) return;
                replaceFirstRow = row;
                replaceFirstCol = col;
                actionMode = ActionMode.REPLACE_CARD_SECOND;
                gameBoardView.setActionGuide("SWAP : Clique sur la 2ème carte à échanger");
                break;
            
            case REPLACE_CARD_SECOND:
                player = gameManager.getCurrentPlayer();
                if (player == null) return;
                if (row == replaceFirstRow && col == replaceFirstCol) return; // même carte
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

                switchToNextPlayer();
                break;
            
            case TRADE_SELECT_OWN_CARD:
                player = gameManager.getCurrentPlayer();
                if (player == null) return;
                CardModel ownCard = player.getHand().getCard(row, col);
                if (ownCard == null) return;
                tradeOwnRow = row;
                tradeOwnCol = col;
                actionMode = ActionMode.TRADE_SELECT_TARGET_PLAYER;
                gameBoardView.setActionGuide("TRADE : Clique un joueur cible a gauche (modifiable)");
                break;

            case TRADE_SELECT_TARGET_PLAYER:
                gameBoardView.setActionGuide("TRADE : Choisis d'abord un joueur a gauche");
                break;

            case TRADE_SELECT_TARGET_CARD:
                completeTrade(row, col);
                break;

            case ALL_PLAYERS_DROP_SELECT_TARGET:
                gameBoardView.setActionGuide("DROP : Choisis un joueur a gauche");
                break;

            case ALL_PLAYERS_DROP_SELECT_CARD:
                applyAllPlayersDropOnSelectedCard(row, col);
                break;

            default:
                break;
        }
    }

    private void selectTradeTargetPlayer(int targetIndex) {
        // on fait le selectTradeTarget seulement si on est en etat TRADE_SELECT_TARGET_PLAYER ou TRADE_SELECT_TARGET_CARD
        // si on est Ni dans l'un Ni dans l'autre on quitte
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

        // checks apres les echnages
        GameRuleModel.removeIdenticalColumns(currentPlayer.getHand(), gameManager.getDiscardPile());
        GameRuleModel.removeIdenticalRows(currentPlayer.getHand(), gameManager.getDiscardPile());
        GameRuleModel.removeIdenticalColumns(targetPlayer.getHand(), gameManager.getDiscardPile());
        GameRuleModel.removeIdenticalRows(targetPlayer.getHand(), gameManager.getDiscardPile());

        System.out.println("TRADE_CARD_WITH_PLAYER: échange [" + tradeOwnRow + "," + tradeOwnCol + "] <-> joueur "
                + tradeTargetPlayerIndex + " [" + targetRow + "," + targetCol + "]");

        actionMode = ActionMode.NONE;
        resetTradeSelection();
        gameBoardView.setActionHandVisible(true);
        gameBoardView.showOtherPlayerMain(false);
        gameBoardView.showPlayerCards(currentPlayer);
        updateAllDecksDisplay();
        gameBoardView.setActionGuide("TRADE terminé !");
        switchToNextPlayer();
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
            switchToNextPlayer();
            return;
        }

        actionMode = ActionMode.ALL_PLAYERS_DROP_SELECT_TARGET;
        gameBoardView.setActionHandVisible(false);
        gameBoardView.showOtherPlayerMain(false);
        gameBoardView.showPlayerCards(currentPlayer);
        gameBoardView.setActionGuide("DROP : Choisis un joueur a gauche (" + allPlayersDropPendingTargets.size() + " restants)");
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
            gameBoardView.setActionGuide("DROP : ce joueur a deja été affecté");
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
        if (allPlayersDropCurrentTargetIndex < 0) return;
        if (!allPlayersDropPendingTargets.contains(allPlayersDropCurrentTargetIndex)) return;

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
        if (drawnCard != null && drawnCard.isFaceDown()) {
            drawnCard.returnCard();
        }
        if (targetCard.isFaceDown()) {
            targetCard.returnCard();
        }

        targetPlayer.getHand().replaceCard(row, col, drawnCard);
        gameManager.getDiscardPile().addCardAtTop(targetCard);

        // checks colonnes et lignes
        GameRuleModel.removeIdenticalColumns(targetPlayer.getHand(), gameManager.getDiscardPile());
        GameRuleModel.removeIdenticalRows(targetPlayer.getHand(), gameManager.getDiscardPile());

        allPlayersDropPendingTargets.remove(Integer.valueOf(allPlayersDropCurrentTargetIndex));
        allPlayersDropCurrentTargetIndex = -1;
        updateAllDecksDisplay();

        if (allPlayersDropPendingTargets.isEmpty()) {
            actionMode = ActionMode.NONE;
            resetAllPlayersDropSelection();
            gameBoardView.setActionHandVisible(true);
            gameBoardView.showOtherPlayerMain(false);
            gameBoardView.showPlayerCards(currentPlayer);
            gameBoardView.setActionGuide("DROP : tous les joueurs ont ete affectés");
            switchToNextPlayer();
            return;
        }

        actionMode = ActionMode.ALL_PLAYERS_DROP_SELECT_TARGET;
        gameBoardView.showPlayerCards(currentPlayer);
        gameBoardView.setActionGuide("DROP : Choisis le prochain joueur (" + allPlayersDropPendingTargets.size() + " restants)");
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

    /**
     * Passe au joueur suivant via GameManager et affiche ses cartes
     */
    public void switchToNextPlayer() {
        actionMode = ActionMode.NONE;
        resetTradeSelection();
        resetAllPlayersDropSelection();
        gameBoardView.setActionHandVisible(true);
        gameBoardView.showOtherPlayerMain(false);

        Timer beforeSwitch = new Timer(1500, e -> {
            nextPlayerSound();
        });
        beforeSwitch.setRepeats(false);
        beforeSwitch.start();
        waitingNextTurn = true;
        gameBoardView.hideCursorCard();
        
        // timer pour voir les cartes avant de changer de main
        Timer delayTimer = new Timer(4000, e -> {
            gameManager.nextPlayer();

            // detecter fin de manche ou fin de partie
            if (gameManager.getGameState() == GameManager.GameState.ROUND_ENDED
                || gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
                waitingNextTurn = false;
                showRoundEndScreen();
                return;
            }

            gameManager.startTurn();
            gameBoardView.showPlayerCards(gameManager.getCurrentPlayer());
            updateAllDecksDisplay();
            refreshGuide();
            waitingNextTurn = false;
            //regarde si c'est un bot
            botController.handleBotTurn();
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
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
                public void mouseClicked(MouseEvent e) { ContinueRound(); }
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
                public void mouseClicked(MouseEvent e) { gameBoardModel.retourMenuCalled(); }
            });
        }
    }

    /**
     * Relance une nouvelle manche apres clic sur Continuer
     */
    private void ContinueRound() {
        gameManager.startNewGame();
        waitingNextTurn = false;
        waitingReveal = false;

        actionMode = ActionMode.NONE;
        resetTradeSelection();
        resetTradeSelection();
        resetAllPlayersDropSelection();

        gameBoardView.showPlayerCards(gameManager.getCurrentPlayer());
        updateAllDecksDisplay();
        Frame frame = gameBoardModel.getFrame();
        frame.showPanel("GAME");
        refreshGuide();
        // Tour du bot
        SwingUtilities.invokeLater(() -> botController.handleBotTurn());
    }

    /**
     * Message d'action indiqunat au joueur quoi faire
     */
    public void refreshGuide() {
        switch (gameManager.getTurnPhase()) {
            case DRAW_DECISION -> gameBoardView.setActionGuide("Choisis : Pioche ou Défausse");
            case CARD_DRAW_FROM_DECK -> gameBoardView.setActionGuide("Echange ou Défausse puis revele une carte");
            case CARD_TAKE_FROM_DISCARD -> gameBoardView.setActionGuide("Clique une carte de ta main pour échanger");
            case CRAD_DISCARD_FROM_DRAW -> gameBoardView.setActionGuide("Veuillez reveler une carte de votre main");
            case TURN_ENDED -> gameBoardView.setActionGuide("Tour terminé");
            case INIT_REVEAL -> gameBoardView.setActionGuide(gameManager.getCurrentPlayer().getName()
                    + " doit réveler 2 cartes (" + gameManager.getInitRevealCount() + ")");
        }
    }

    public void nextPlayerSound() {
        audio.playSfx("/sfx/round.wav"); //je trouve le son bizzare hmm j'ai mis un alternatif ou juste avec ce son c'est bizzare d'etre accompagné d'un fade
        nextRoundPanel.setCurrentPlayer(gameManager.getCurrentPlayer());
        // nextRoundPanel.fade(true);
        // waitingNextTurn = true;
        // Timer waitTimer = new Timer(2500, e -> {
        //     nextRoundPanel.fade(false);
        //     waitingNextTurn = false;
        // });
        // waitTimer.setRepeats(false);
        // waitTimer.start();
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
    private void updateAllDecksDisplay() {
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
}
