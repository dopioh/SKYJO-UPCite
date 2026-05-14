package fr.upc.cr1a.controller;

import java.util.ArrayList;

import javax.swing.Timer;

import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolServer;
import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.BotHandModel;
import fr.upc.cr1a.model.BotLow;
import fr.upc.cr1a.model.BotModel;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GameRuleModel;
import fr.upc.cr1a.model.GameSession;
import fr.upc.cr1a.model.NumberCardModel;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.model.interfaces.BotStrategyInterface;
import fr.upc.cr1a.view.GameBoardView;

/**
 * Gère le tour automatique lorsqu'un joueur humain dépasse le temps imparti.
 * Réutilise les décisions des bots existants (Low pour le moment) #################Jtrouve pas le bot normal
 */
public class TimeoutTurnController {

    private static final int AUTO_ACTION_DELAY_MS = 800;

    private final GameBoardView gameBoardView;
    private final GameManager gameManager;
    private final GameBoardController mainController;
    private final GameLoop gameLoop;

    public TimeoutTurnController(GameBoardView gameBoardView,GameManager gameManager,GameBoardController mainController,GameLoop gameLoop) {
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.mainController = mainController;
        this.gameLoop = gameLoop;
    }

    /**
     * Renvoie le NetworkProtocolServer si on est host, sinon null. Utilise pour broadcaster
     * les coups joues par l'auto-play AFK afin de synchroniser les clients.
     */
    private NetworkProtocolServer hostServer() {
        if (GameSession.status != GameSession.STATUS.HOST) return null;
        try {
            fr.upc.cr1a.controller.network.Host h =
                gameManager.getGameBoardModel().getFrame().getLobbyRoomController().getHost();
            return (h != null) ? h.getGameHost() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void broadcastCardFlippedIfHost(String playerId, int col, int row, CardModel card) {
        NetworkProtocolServer s = hostServer();
        if (s == null) return;
        int val = (card instanceof NumberCardModel) ? ((NumberCardModel) card).getCardValue() : -1;
        s.broadcastCardFlipped(playerId, col, row, val);
    }

    private void broadcastTurnPhaseIfHost() {
        NetworkProtocolServer s = hostServer();
        if (s == null) return;
        PlayerModel cp = gameManager.getCurrentPlayer();
        if (cp == null) return;
        s.broadcastTurnChanged(cp.getPlayerData().getID(), gameManager.getTurnPhase().name());
    }

    private int valueOf(CardModel c) {
        return (c instanceof NumberCardModel) ? ((NumberCardModel) c).getCardValue() : -1;
    }

    /**
     * Appelé par le GameLoop quand le temps du joueur courant est écoulé.
     */
    public void handleTimeoutForCurrentPlayer() {
        if (!gameLoop.isGameActive()) return;

        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        if (currentPlayer == null) return;
        if (currentPlayer instanceof BotModel) return;

        BotStrategyInterface strategy = new BotLow(3);

        switch (gameManager.getTurnPhase()) {
            case INIT_REVEAL -> handleTimeoutInitReveal(currentPlayer);
            case DRAW_DECISION -> handleTimeoutDrawDecision(currentPlayer, strategy);
            case CARD_DRAW_FROM_DECK -> handleTimeoutAfterDraw(currentPlayer, strategy);
            case CARD_TAKE_FROM_DISCARD -> handleTimeoutAfterTakeDiscard(currentPlayer, strategy);
            case CARD_DISCARD_FROM_DRAW -> handleTimeoutRevealAfterDiscard(currentPlayer);
            default -> mainController.requestEndTurnFromBot();
        }
    }

    /**
     * Copie la main du joueur humain dans un BotHandModel pour pouvoir
     * réutiliser les stratégies de bot
     */
    private BotHandModel copyToBotHand(PlayerModel player){
        BotHandModel botHand = new BotHandModel();
        for (int row = 0; row < player.getHand().getRows(); row++){
            for (int col = 0; col < player.getHand().getColumns(); col++){
                CardModel card = player.getHand().getCard(row, col);
                if (card != null) {
                    botHand.placeCard(row, col, card);
                }
            }
        }
        return botHand;
    }

    /**
     * Gère le reveal automatique pendant l'initialisation.
     * Ici on révèle simplement des cartes cachées au hasard.
     */
    private void handleTimeoutInitReveal(PlayerModel displayedPlayer) {
        if (!gameLoop.isGameActive()) return;
        int[] pos = findRandomHiddenCard(displayedPlayer);
        if (pos == null) {
            mainController.requestEndTurnFromBot();
            return;
        }
        // capturer l'id avant revealInitCard car le joueur courant peut changer apres 2 reveals
        String revealingId = displayedPlayer.getPlayerData().getID();
        boolean ok = gameManager.revealInitCard(pos[1], pos[0]);
        if (!ok) {
            mainController.requestEndTurnFromBot();
            return;
        }
        // Broadcast UPDATE_CARD_FLIPPED pour que les clients voient la carte revealee
        CardModel revealedCard = displayedPlayer.getHand().getCard(pos[0], pos[1]);
        broadcastCardFlippedIfHost(revealingId, pos[1], pos[0], revealedCard);

        gameBoardView.showPlayerCards(displayedPlayer);
        mainController.updateAllDecksDisplay();
        mainController.refreshGuide();

        if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL
                && gameManager.getInitRevealCount() == 1) {
            runLater(AUTO_ACTION_DELAY_MS, () -> handleTimeoutInitReveal(displayedPlayer));
            return;
        }
        mainController.requestEndTurnFromBot();
    }

    /**
     * Gère le timeout quand le joueur doit décider entre pioche et défausse.
     * On réutilise la stratégie du bot.
     */
    private void handleTimeoutDrawDecision(PlayerModel player, BotStrategyInterface strategy) {
        if (!gameLoop.isGameActive()) return;

        BotHandModel botHand = copyToBotHand(player);
        CardModel discardCard = gameManager.getDiscardPile().isEmpty()? null : gameManager.getDiscardPile().getTopCard();

        boolean takeDiscard = discardCard != null && strategy.TakeFromDiscard(botHand, discardCard);

        String pid = player.getPlayerData().getID();
        if (takeDiscard) {
            CardModel taken = gameManager.takeDiscard();
            // Broadcast aux clients : carte prise dans la defausse + nouvelle phase
            NetworkProtocolServer s = hostServer();
            if (s != null) {
                s.broadcastUpdateDiscardTaken(pid);
                s.broadcastTurnChanged(pid, gameManager.getTurnPhase().name());
            }
            String takenPath = gameBoardView.getCardImagePath(taken);
            gameBoardView.getBtnDefausse().setCardImage(takenPath);
            gameBoardView.showCursorCard(takenPath);
            mainController.updateAllDecksDisplay();
            mainController.refreshGuide();
            runLater(AUTO_ACTION_DELAY_MS, () -> handleTimeoutAfterTakeDiscard(player, strategy));
            return;
        }

        CardModel drawn = gameManager.drawDeck();
        if (drawn.isFaceDown()) {
            drawn.returnCard();
        }
        broadcastTurnPhaseIfHost();

        String drawnPath = gameBoardView.getCardImagePath(drawn);
        gameBoardView.getBtnPioche().setCardImage(drawnPath);
        gameBoardView.showCursorCard(drawnPath);
        gameBoardView.updateDiscardOnly(gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard());
        mainController.refreshGuide();

        runLater(AUTO_ACTION_DELAY_MS, () -> handleTimeoutAfterDraw(player, strategy));
    }

    /**
     * Après avoir pioché
     */
    private void handleTimeoutAfterDraw(PlayerModel player, BotStrategyInterface strategy) {
        if (!gameLoop.isGameActive()) return;

        BotHandModel botHand = copyToBotHand(player);
        CardModel drawCard = gameManager.getCurrentDrawCard();
        if (drawCard == null) {
            mainController.requestEndTurnFromBot();
            return;
        }

        boolean exchange = strategy.ExchangeDrawCard(botHand, drawCard);
        String pid = player.getPlayerData().getID();

        if (exchange) {
            int[] position = strategy.CardToExchange(botHand, drawCard);

            if (position[0] != -1 && position[1] != -1) {
                int col = position[0];
                int row = position[1];
                int newValue = valueOf(drawCard);
                CardModel oldCard = player.getHand().getCard(row, col);
                int discardedValue = valueOf(oldCard);

                gameManager.exchangeCardInHand(col, row);
                // Broadcast aux clients
                NetworkProtocolServer s = hostServer();
                if (s != null) {
                    s.broadcastExchange(pid, col, row, newValue, discardedValue);
                }
                gameBoardView.hideCursorCard();
                gameBoardView.showPlayerCards(player);
                mainController.updateAllDecksDisplay();
                mainController.refreshGuide();
                mainController.requestEndTurnFromBot();
                return;
            }
        }

        int discardedValue = valueOf(drawCard);
        gameManager.discardDrawCard();
        // Broadcast aux clients : carte piochee jetee + nouvelle phase (CARD_DISCARD_FROM_DRAW)
        NetworkProtocolServer s = hostServer();
        if (s != null) {
            s.broadcastUpdateDiscardDraw(pid, discardedValue);
            s.broadcastTurnChanged(pid, gameManager.getTurnPhase().name());
        }
        gameBoardView.hideCursorCard();
        mainController.updateAllDecksDisplay();
        mainController.refreshGuide();

        runLater(AUTO_ACTION_DELAY_MS, () -> handleTimeoutRevealAfterDiscard(player));
    }

    /**
     * Après avoir pris dans la défausse
     */
    private void handleTimeoutAfterTakeDiscard(PlayerModel player, BotStrategyInterface strategy) {
        if (!gameLoop.isGameActive()) return;

        BotHandModel botHand = copyToBotHand(player);
        CardModel drawCard = gameManager.getCurrentDrawCard();
        if (drawCard == null) {
            mainController.requestEndTurnFromBot();
            return;
        }

        int[] position = strategy.CardToExchange(botHand, drawCard);
        String pid = player.getPlayerData().getID();

        if (position[0] != -1 && position[1] != -1) {
            int col = position[0];
            int row = position[1];
            int newValue = valueOf(drawCard);
            CardModel oldCard = player.getHand().getCard(row, col);
            int discardedValue = valueOf(oldCard);

            gameManager.exchangeCardInHand(col, row);
            NetworkProtocolServer s = hostServer();
            if (s != null) {
                s.broadcastExchange(pid, col, row, newValue, discardedValue);
            }
        }

        gameBoardView.hideCursorCard();
        gameBoardView.showPlayerCards(player);
        mainController.updateAllDecksDisplay();
        mainController.refreshGuide();
        mainController.requestEndTurnFromBot();
    }

    /**
     * Après défausse, reveal card
     */
    private void handleTimeoutRevealAfterDiscard(PlayerModel player) {
        if (!gameLoop.isGameActive()) return;

        int[] pos = findRandomHiddenCard(player);
        if (pos != null) {
            player.getHand().revealCard(pos[0], pos[1]);
            CardModel revealedCard = player.getHand().getCard(pos[0], pos[1]);
            // Broadcast UPDATE_CARD_FLIPPED pour que les clients voient la carte revealee
            broadcastCardFlippedIfHost(player.getPlayerData().getID(), pos[1], pos[0], revealedCard);
            if (GameManager.isStarCard(revealedCard)) {
                ActionCardModel actionCard = gameManager.grantActionForStar(player);
                if (actionCard != null) {
                    System.out.println(player.getName() + " révèle une étoile et gagne une action: " + actionCard.getType());
                }
            }
            GameRuleModel.removeIdenticalColumns(player.getHand(), gameManager.getDiscardPile());
            GameRuleModel.removeIdenticalRows(player.getHand(), gameManager.getDiscardPile());

            //verifier si fin de manche
            gameManager.checkAndTriggerLastRound();
        }

        gameBoardView.showPlayerCards(player);
        mainController.updateAllDecksDisplay();
        mainController.refreshGuide();
        mainController.requestEndTurnFromBot();
    }

    /**
     * Retourne une carte aléatoire
     */
    private int[] findRandomHiddenCard(PlayerModel player) {
        ArrayList<int[]> hiddenCards = new ArrayList<>();

        for (int row = 0; row < player.getHand().getRows(); row++) {
            for (int col = 0; col < player.getHand().getColumns(); col++) {
                CardModel card = player.getHand().getCard(row, col);
                if (card != null && card.isFaceDown()) {
                    hiddenCards.add(new int[] { row, col });
                }
            }
        }

        if (hiddenCards.isEmpty()) {
            return null;
        }

        int index = (int) (Math.random() * hiddenCards.size());
        return hiddenCards.get(index);
    }

    /**
     * Délai visuel
     */
    private void runLater(int delayMs, Runnable action) {
        Timer timer = new Timer(delayMs, e -> {
            if (!gameLoop.isGameActive()) return;
            action.run();
        });
        timer.setRepeats(false);
        timer.start();
    }
}