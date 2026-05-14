package fr.upc.cr1a.controller;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolServer;
import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.BotHandModel;
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
 * gere les actions automatiques des bots
 *
 * @author Billy
 */
public class GameBoardBotController {

    private GameBoardView gameBoardView;
    private GameManager gameManager;
    private GameBoardController mainController;

    private List<Timer> activeTimers = new ArrayList<>();
    private boolean pausedState = false;

    public GameBoardBotController(GameBoardView gameBoardView, GameManager gameManager, GameBoardController mainController) {
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.mainController = mainController;
    }

    /**
     * Renvoie l'instance NetworkProtocolServer (GameHost) si l'on est host, sinon null.
     * Utilise pour relayer aux clients les actions jouees par les bots.
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

    private void broadcastTurnChangedIfHost() {
        NetworkProtocolServer s = hostServer();
        if (s == null) return;
        PlayerModel cp = gameManager.getCurrentPlayer();
        if (cp == null) return;
        s.broadcastTurnChanged(cp.getPlayerData().getID(), gameManager.getTurnPhase().name());
    }

    /**
     *arrête toutes les actions des bots en cours
     */
    public void stopAllBotActions() {
        for (Timer timer : activeTimers) {
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
        }
        activeTimers.clear();
        pausedState = true;
    }

    /**
     *reprend les actions des bots
     */
    public void resumeBotActions() {
        pausedState = false;
    }

    /**
     *regarde si c'est un bot et lance son tour
     */
    public void handleBotTurn() {
        //seul le host pilote les bots ; les clients recoivent les broadcasts.
        if (GameSession.status == GameSession.STATUS.CLIENT) return;

        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        //rien faire si c'est pas un bot
        if (!(currentPlayer instanceof BotModel)) {
            return;
        }

        BotModel bot = (BotModel) currentPlayer;

        //le bot peut jouer une action en début de tour
        if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
            if (gameManager.withActionCard() && !bot.getActionHand().isEmpty()) {
                if (shouldBotPlayAction(bot)) {
                    handleBotPlayAction(bot);
                    return;
                }
            }
        }

        //révéle 2 cartes
        if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) {
            handleBotInit(bot);
            return;
        }

        //pioche ou prends de la défausse
        if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
            handleBotDrawDecision(bot);
            return;
        }

        //apres pioche : échanger ou défausser
        if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
            handleBotAfterDraw(bot);
            return;
        }

        //apres défausse, doit échanger
        if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD) {
            handleBotAfterTakeDiscard(bot);
            return;
        }
    }

    /**
     *initialisation du bot, montrer 2 cartes
     */
    private void handleBotInit(BotModel bot) {
        if (pausedState) return;

        mainController.setWaitingNextTurn(true);

        //montre la 1ère carte
        Timer firstCardTimer = new Timer(800, e -> {
            if (pausedState) return;

            BotHandModel hand = bot.getHand();

            //retourner une carte aléatoire
            int row, col;
            do {
                row = (int) (Math.random() * 3);
                col = (int) (Math.random() * 4);
            } while (!hand.getCard(row, col).isFaceDown());

            gameManager.revealInitCard(col, row);
            CardModel firstRevealed = hand.getCard(row, col);
            if (GameManager.isStarCard(firstRevealed)) {
                grantActionForStar(bot);
                mainController.updateAllDecksDisplay();
            }
            // Broadcast multi et appliquent la revelation
            broadcastCardFlippedIfHost(bot.getPlayerData().getID(), col, row, firstRevealed);
            broadcastTurnChangedIfHost();
            gameBoardView.flipCardAt(row, col, () -> gameBoardView.showPlayerCards(bot));
            mainController.updateAllDecksDisplay();
            mainController.refreshGuide();

            //revéler la 2ème
            if (gameManager.getInitRevealCount() < 2) {
                Timer secondCardTimer = new Timer(800, ev -> {
                    if (pausedState) return;

                    int row2, col2;
                    do {
                        row2 = (int) (Math.random() * 3);
                        col2 = (int) (Math.random() * 4);
                    } while (!hand.getCard(row2, col2).isFaceDown());

                    gameManager.revealInitCard(col2, row2);
                    CardModel secondRevealed = hand.getCard(row2, col2);
                    if (GameManager.isStarCard(secondRevealed)) {
                        grantActionForStar(bot);
                    }
                    // Broadcast multi
                    broadcastCardFlippedIfHost(bot.getPlayerData().getID(), col2, row2, secondRevealed);
                    broadcastTurnChangedIfHost();
                    gameBoardView.flipCardAt(row2, col2, () -> gameBoardView.showPlayerCards(bot));
                    mainController.updateAllDecksDisplay();
                    mainController.refreshGuide();
                    mainController.setWaitingNextTurn(false);
                    mainController.requestEndTurnFromBot();
                });
                secondCardTimer.setRepeats(false);
                activeTimers.add(secondCardTimer);
                secondCardTimer.start();
            } else {
                mainController.setWaitingNextTurn(false);
                mainController.requestEndTurnFromBot();
            }
        });
        firstCardTimer.setRepeats(false);
        activeTimers.add(firstCardTimer);
        firstCardTimer.start();
    }

    /**
     * Le bot décide s'il pioche ou prend de la défausse
     */
    private void handleBotDrawDecision(BotModel bot) {
        if (pausedState) return;

        mainController.setWaitingNextTurn(true);

        Timer decisionTimer = new Timer(800, e -> {
            if (pausedState) return;

            BotHandModel botHand = bot.getHand();
            BotStrategyInterface strategy = bot.getStrategy();

            //cherche la strat du bot
            if (strategy == null) {
                System.err.println("Erreur: Le bot " + bot.getName() + " n'a pas de stratégie!");
                mainController.setWaitingNextTurn(false);
                mainController.requestEndTurnFromBot();
                return;
            }

            CardModel discardCard = gameManager.getDiscardPile().getTopCard();
            boolean takeDiscard = strategy.TakeFromDiscard(botHand, discardCard);

            if (takeDiscard && discardCard != null) {
                //prend de la défausse
                mainController.getAudio().playSfx("/sfx/defausse.wav");
                gameBoardView.getBtnDefausse().lift();
                CardModel taken = gameManager.takeDiscard();
                // Broadcast multi : le client voit la defausse retiree + la phase CARD_TAKE_FROM_DISCARD
                NetworkProtocolServer srv = hostServer();
                if (srv != null) {
                    srv.broadcastUpdateDiscardTaken(bot.getPlayerData().getID());
                    broadcastTurnChangedIfHost();
                }
                mainController.refreshGuide();
                mainController.setWaitingNextTurn(false);

                //bot échange
                handleBotTurn();
            } else {
                //piocher
                CardModel drawn = gameManager.drawDeck();
                if (drawn.isFaceDown())
                    drawn.returnCard();
                if (GameManager.isStarCard(drawn)) {
                    grantActionForStar(bot);
                }
                // Broadcast multi : on propage seulement la phase (la valeur piochee reste privee au bot/host)
                broadcastTurnChangedIfHost();
                String drawnPath = gameBoardView.getCardImagePath(drawn);
                if (gameBoardView.isHandCardAnimationsEnabled()) {
                    gameBoardView.getBtnPioche().flip(() -> gameBoardView.getBtnPioche().setCardImage(drawnPath));
                } else {
                    gameBoardView.getBtnPioche().setCardImage(drawnPath);
                }
                mainController.updateAllDecksDisplay();
                System.out.println(bot.getName() + " pioche : " + drawn.getCardValue());
                mainController.refreshGuide();
                mainController.setWaitingNextTurn(false);

                //décide d'échanger ou défausser
                Timer showCardTimer = new Timer(1000, ev -> {
                    if (pausedState) return;
                    handleBotTurn();
                });
                showCardTimer.setRepeats(false);
                activeTimers.add(showCardTimer);
                showCardTimer.start();
            }
        });
        decisionTimer.setRepeats(false);
        activeTimers.add(decisionTimer);
        decisionTimer.start();
    }

    /**
     * Le bot décide d'échanger ou de défausser après avoir pioché
     */
    private void handleBotAfterDraw(BotModel bot) {
        if (pausedState) return;

        mainController.setWaitingNextTurn(true);

        Timer exchangeTimer = new Timer(600, e -> {
            if (pausedState) return;

            BotHandModel botHand = bot.getHand();
            BotStrategyInterface strategy = bot.getStrategy();
            CardModel drawCard = gameManager.getCurrentDrawCard();

            boolean exchange = strategy.ExchangeDrawCard(botHand, drawCard);

            if (exchange) {
                //échange après pioche
                int[] position = strategy.CardToExchange(botHand, drawCard);
                if (position[0] != -1 && position[1] != -1) {
                    int col = position[0];
                    int row = position[1];
                    int newValue = (drawCard instanceof NumberCardModel)
                            ? ((NumberCardModel) drawCard).getCardValue() : -1;
                    CardModel oldHand = botHand.getCard(row, col);
                    int discardedValue = (oldHand instanceof NumberCardModel)
                            ? ((NumberCardModel) oldHand).getCardValue() : -1;

                    mainController.getAudio().playSfx("/sfx/card_click.wav");
                    gameManager.exchangeCardInHand(col, row);
                    System.out.println(bot.getName() + " échange [" + row + "," + col + "]");

                    NetworkProtocolServer srv = hostServer();
                    if (srv != null) {
                        srv.broadcastExchange(bot.getPlayerData().getID(), col, row, newValue, discardedValue);
                    }

                    gameBoardView.showPlayerCards(bot);
                    gameBoardView.animatePressAt(row, col);
                    gameBoardView.updateCardDisplay(
                            gameManager.getDiscardPile().getTopCard(),
                            gameManager.getDeck().stackSize() > 0);
                    mainController.refreshGuide();
                    mainController.setWaitingNextTurn(false);
                    mainController.requestEndTurnFromBot();
                }
            } else {
                //jeter la carte piochée puis révéler une carte
                int discardedValue = (drawCard instanceof NumberCardModel)? ((NumberCardModel) drawCard).getCardValue() : -1;
                mainController.getAudio().playSfx("/sfx/defausse.wav");
                gameManager.discardDrawCard();
                // Broadcast multi : defausse mise a jour + nouvelle phase CARD_DISCARD_FROM_DRAW
                NetworkProtocolServer srv = hostServer();
                if (srv != null) {
                    srv.broadcastUpdateDiscardDraw(bot.getPlayerData().getID(), discardedValue);
                    broadcastTurnChangedIfHost();
                }
                gameBoardView.getBtnDefausse().animatePress();
                gameBoardView.updateCardDisplay(
                        gameManager.getDiscardPile().getTopCard(),
                        gameManager.getDeck().stackSize() > 0);
                System.out.println(bot.getName() + " défausse et va révéler une carte");
                mainController.refreshGuide();

                //revele une carte aléatoire de sa main
                Timer revealTimer = new Timer(800, ev -> {
                    if (pausedState) return;

                    int[] pos = revealRandomFaceDownCard(botHand);
                    CardModel revealedCard = botHand.getCard(pos[0], pos[1]);
                    if (GameManager.isStarCard(revealedCard)) {
                        grantActionForStar(bot);
                    }

                    // Broadcast de la revelation [pos = [row, col]]
                    broadcastCardFlippedIfHost(bot.getPlayerData().getID(), pos[1], pos[0], revealedCard);

                    //regarde et supprimer les colonnes/lignes identiques
                    GameRuleModel.removeIdenticalColumns(botHand, gameManager.getDiscardPile(), bot);
                    GameRuleModel.removeIdenticalRows(botHand, gameManager.getDiscardPile(), bot);

                    //vérifier si fin de manche
                    gameManager.checkAndTriggerLastRound();

                    gameBoardView.flipCardAt(pos[0], pos[1], () -> gameBoardView.showPlayerCards(bot));
                    mainController.updateAllDecksDisplay();
                    System.out.println(bot.getName() + " révèle une carte");
                    mainController.setWaitingNextTurn(false);
                    mainController.requestEndTurnFromBot();
                });
                revealTimer.setRepeats(false);
                activeTimers.add(revealTimer);
                revealTimer.start();
            }
        });
        exchangeTimer.setRepeats(false);
        activeTimers.add(exchangeTimer);
        exchangeTimer.start();
    }

    /**
     * Le bot échange une carte après avoir pris de la défausse
     */
    private void handleBotAfterTakeDiscard(BotModel bot) {
        if (pausedState) return;

        mainController.setWaitingNextTurn(true);

        Timer exchangeTimer = new Timer(800, e -> {
            if (pausedState) return;

            BotHandModel botHand = bot.getHand();
            BotStrategyInterface strategy = bot.getStrategy();
            CardModel drawCard = gameManager.getCurrentDrawCard();

            //vérifier que drawCard n'est pas null avant l'échange
            if (drawCard == null) {
                mainController.setWaitingNextTurn(false);
                mainController.requestEndTurnFromBot();
                return;
            }

            //doit échanger une carte
            int[] position = strategy.CardToExchange(botHand, drawCard);
            if (position[0] != -1 && position[1] != -1) {
                int col = position[0];
                int row = position[1];
                int newValue = (drawCard instanceof NumberCardModel)? ((NumberCardModel) drawCard).getCardValue() : -1;
                CardModel oldHand = botHand.getCard(row, col);
                int discardedValue = (oldHand instanceof NumberCardModel)? ((NumberCardModel) oldHand).getCardValue() : -1;

                mainController.getAudio().playSfx("/sfx/card_click.wav");
                gameManager.exchangeCardInHand(col, row);
                System.out.println(bot.getName() + " échange [" + row + "," + col + "]");

                NetworkProtocolServer srv = hostServer();
                if (srv != null) {
                    srv.broadcastExchange(bot.getPlayerData().getID(), col, row, newValue, discardedValue);
                }

                gameBoardView.showPlayerCards(bot);
                gameBoardView.animatePressAt(row, col);
                gameBoardView.getBtnDefausse().resetLift();
                gameBoardView.updateCardDisplay(gameManager.getDiscardPile().getTopCard(),gameManager.getDeck().stackSize() > 0);
                mainController.refreshGuide();
            }
            mainController.setWaitingNextTurn(false);
            mainController.requestEndTurnFromBot();
        });
        exchangeTimer.setRepeats(false);
        activeTimers.add(exchangeTimer);
        exchangeTimer.start();
    }

    // retourne [row, col] de la carte révélée
    private int[] revealRandomFaceDownCard(BotHandModel hand) {
        int row;
        int col;
        do {
            row = (int) (Math.random() * 3);
            col = (int) (Math.random() * 4);
        } while (hand.getCard(row, col) == null || !hand.getCard(row, col).isFaceDown());

        hand.revealCard(row, col);
        return new int[]{ row, col };
    }

    private void grantActionForStar(PlayerModel player) {
        ActionCardModel actionCard = gameManager.grantActionForStar(player);
        if (actionCard == null) {
            return;
        }
    }

    /**
     * verifie si le bot doit jouer une action
     */
    private boolean shouldBotPlayAction(BotModel bot) {
        BotStrategyInterface strategy = bot.getStrategy();
        if (strategy == null) {
            return Math.random() < 0.5;
        }
        return strategy.shouldPlayAction();
    }

    /**
     * gère le jeu d'une carte action par le bot
     */
    private void handleBotPlayAction(BotModel bot) {
        if (pausedState) return;

        mainController.setWaitingNextTurn(true);

        Timer actionTimer = new Timer(800, e -> {
            if (pausedState) return;

            if (bot.getActionHand().isEmpty()) {
                mainController.setWaitingNextTurn(false);
                handleBotTurn();
                return;
            }

            //choisir aléatoirement une action
            int actionIndex = (int) (Math.random() * bot.getActionHand().size());
            ActionType actionType = bot.getActionHand().get(actionIndex);

            System.out.println(bot.getName() + " joue l'action: " + actionType);


            try {
                switch (actionType) {
                    case PLAY_TWO_TURN:
                        gameManager.grantExtraTurn();
                        System.out.println("[BOT INFO] GrandExtraTurn Called");
                        //gameManager.grantExtraTurn();
                        //handleBotTurn();
                        break;

                    case REPLACE_CARD_IN_HAND:
                        botReplaceCardInHand(bot);
                        break;

                    case TRADE_CARD_WITH_PLAYER:
                        botTradeCardWithPlayer(bot);
                        break;

                    case SPY_ONE_COLUMN:
                        botSpyOneColumn(bot);
                        break;

                    case DRAW_THREE_CARD:
                        botDrawThreeCard(bot);
                        break;

                    case PLAY_LAST_ACTION_IN_DEFAUSSE:
                        botPlayLastActionInDefausse(bot);
                        break;

                    case ALL_PLAYERS_DROP_CARD:
                        botAllPlayersDropCard(bot);
                        break;

                    default:
                        break;
                }

                bot.getActionHand().remove(actionIndex);
                gameManager.getDiscardActionPile().addCardAtTop(new ActionCardModel(false, actionType));
                gameBoardView.showPlayerCards(bot);
                mainController.updateAllDecksDisplay();

            } catch (Exception ex) {
                System.err.println("Erreur lors du jeu de l'action: " + ex.getMessage());
            }

            mainController.setWaitingNextTurn(false);
            mainController.requestEndTurnFromBot();
        });
        actionTimer.setRepeats(false);
        activeTimers.add(actionTimer);
        actionTimer.start();
    }

    /**
     *bot joue REPLACE_CARD_IN_HAND
     */
    private void botReplaceCardInHand(BotModel bot) {
        BotHandModel hand = bot.getHand();
        int row1, col1, row2, col2;
        do {
            row1 = (int) (Math.random() * 3);
            col1 = (int) (Math.random() * 4);
            row2 = (int) (Math.random() * 3);
            col2 = (int) (Math.random() * 4);
        } while ((row1 == row2 && col1 == col2) || hand.getCard(row1, col1) == null || hand.getCard(row2, col2) == null);

        CardModel card1 = hand.getCard(row1, col1);
        CardModel card2 = hand.getCard(row2, col2);

        mainController.getAudio().playSfx("/sfx/teleport.wav");
        hand.replaceCard(row1, col1, card2);
        hand.replaceCard(row2, col2, card1);
    }

    /**
     * bot joue TRADE_CARD_WITH_PLAYER
     */
    private void botTradeCardWithPlayer(BotModel bot) {
        ArrayList<PlayerModel> players = new ArrayList<>(gameManager.getPlayers());
        int botIndex = players.indexOf(bot);

        int targetIndex;
        do {
            targetIndex = (int) (Math.random() * players.size());
        } while (targetIndex == botIndex);

        PlayerModel target = players.get(targetIndex);
        
        // Préférer les cartes face down du bot
        int myRow, myCol;
        do {
            myRow = (int) (Math.random() * 3);
            myCol = (int) (Math.random() * 4);
        } while (bot.getHand().getCard(myRow, myCol) == null);
        
        // Prendre une position aléatoire du joueur adverse
        int opRow = (int) (Math.random() * 3);
        int opCol = (int) (Math.random() * 4);

        CardModel myCard = bot.getHand().getCard(myRow, myCol);
        CardModel opCard = target.getHand().getCard(opRow, opCol);

        if (myCard != null && opCard != null) {
            mainController.getAudio().playSfx("/sfx/teleport.wav");
            bot.getHand().replaceCard(myRow, myCol, opCard);
            target.getHand().replaceCard(opRow, opCol, myCard);
        }
    }

    /**
     * bot joue SPY_ONE_COLUMN
     */
    private void botSpyOneColumn(BotModel bot) {
        // Le bot n'a pas besoin de faire quelque chose avec l'info qu'il regarde
        // On termine directement le tour
        mainController.setWaitingNextTurn(false);
        mainController.requestEndTurnFromBot();
    }

    /**
     * bot joue DRAW_THREE_CARD
     */
    private void botDrawThreeCard(BotModel bot) {
        CardModel card1 = gameManager.drawDeck();
        CardModel card2 = gameManager.drawDeck();
        CardModel card3 = gameManager.drawDeck();

        if (card1.isFaceDown()) card1.returnCard();
        if (card2.isFaceDown()) card2.returnCard();
        if (card3.isFaceDown()) card3.returnCard();

        if (GameManager.isStarCard(card1)) grantActionForStar(bot);
        if (GameManager.isStarCard(card2)) grantActionForStar(bot);
        if (GameManager.isStarCard(card3)) grantActionForStar(bot);

        //choisir aléatoirement une carte ou aucune
        int choice = (int) (Math.random() * 4);

        CardModel chosenCard = null;
        CardModel discardCard1 = card1;
        CardModel discardCard2 = card2;
        CardModel discardCard3 = card3;

        switch (choice) {
            case 0:
                chosenCard = card1;
                discardCard1 = card2;
                discardCard2 = card3;
                discardCard3 = null;
                break;
            case 1:
                chosenCard = card2;
                discardCard1 = card1;
                discardCard2 = card3;
                discardCard3 = null;
                break;
            case 2:
                chosenCard = card3;
                discardCard1 = card1;
                discardCard2 = card2;
                discardCard3 = null;
                break;
            default:
                discardCard1 = card1;
                discardCard2 = card2;
                discardCard3 = card3;
                break;
        }

        if (chosenCard != null) {
            //choisir une position pour échanger la carte
            int row = (int) (Math.random() * 3);
            int col = (int) (Math.random() * 4);
            CardModel oldCard = bot.getHand().getCard(row, col);

            if (oldCard != null) {
                bot.getHand().replaceCard(row, col, chosenCard);
                gameManager.getDiscardPile().addCardAtTop(oldCard);
            }
        }

        if (discardCard1 != null) gameManager.getDiscardPile().addCardAtTop(discardCard1);
        if (discardCard2 != null) gameManager.getDiscardPile().addCardAtTop(discardCard2);
        if (discardCard3 != null) gameManager.getDiscardPile().addCardAtTop(discardCard3);
    }

    /**
     * bot joue PLAY_LAST_ACTION_IN_DEFAUSSE
     */
    private void botPlayLastActionInDefausse(BotModel bot) {
        if (gameManager.getDiscardActionPile().getTopCard() != null) {
            ActionCardModel topCard = (ActionCardModel) gameManager.getDiscardActionPile().getTopCard();
            bot.getActionHand().add(topCard.getType());
            gameManager.getDiscardActionPile().removeCardAtTop();
        }
    }

    /**
     * bot joue ALL_PLAYERS_DROP_CARD
     */
    private void botAllPlayersDropCard(BotModel bot) {
        ArrayList<PlayerModel> players = new ArrayList<>(gameManager.getPlayers());
        int botIndex = players.indexOf(bot);

        for (int i = 0; i < players.size(); i++) {
            if (i == botIndex) continue;

            PlayerModel target = players.get(i);
            int row = (int) (Math.random() * 3);
            int col = (int) (Math.random() * 4);

            CardModel targetCard = target.getHand().getCard(row, col);
            if (targetCard == null) continue;

            CardModel drawnCard = gameManager.drawDeck();
            if (drawnCard.isFaceDown()) drawnCard.returnCard();

            target.getHand().replaceCard(row, col, drawnCard);
            gameManager.getDiscardPile().addCardAtTop(targetCard);

            if (GameManager.isStarCard(drawnCard)) {
                grantActionForStar(target);
            }

            GameRuleModel.removeIdenticalColumns(target.getHand(), gameManager.getDiscardPile(), target);
            GameRuleModel.removeIdenticalRows(target.getHand(), gameManager.getDiscardPile(), target);

            //verifier si fin de manche
            gameManager.checkAndTriggerLastRound();
        }
    }
}
