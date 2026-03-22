package fr.upc.cr1a.controller;

import javax.swing.Timer;

import fr.upc.cr1a.model.BotHandModel;
import fr.upc.cr1a.model.BotLow;
import fr.upc.cr1a.model.BotModel;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GameRuleModel;
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

    public GameBoardBotController(GameBoardView gameBoardView, GameManager gameManager, GameBoardController mainController) {
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.mainController = mainController;
    }

    /**
     *regarde si c'est un bot et lance son tour
     */
    public void handleBotTurn() {
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        //rien faire si c'est pas un bot
        if (!(currentPlayer instanceof BotModel)) {
            return;
        }

        BotModel bot = (BotModel) currentPlayer;

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
        mainController.setWaitingNextTurn(true);

        //montre la 1ère carte
        Timer firstCardTimer = new Timer(800, e -> { // timer pour plus de fluidité
            BotHandModel hand = bot.getHand();

            //retourner une carte aléatoire
            int row, col;
            do {
                row = (int) (Math.random() * 3);
                col = (int) (Math.random() * 4);
            } while (!hand.getCard(row, col).isFaceDown());

            gameManager.revealInitCard(col, row);
            gameBoardView.showPlayerCards(bot);
            mainController.updateAllDecksDisplay();
            mainController.refreshGuide();

            //revéler la 2ème
            if (gameManager.getInitRevealCount() < 2) {
                Timer secondCardTimer = new Timer(800, ev -> { // fluidité
                    int row2, col2;
                    do {
                        row2 = (int) (Math.random() * 3);
                        col2 = (int) (Math.random() * 4);
                    } while (!hand.getCard(row2, col2).isFaceDown());

                    gameManager.revealInitCard(col2, row2);
                    gameBoardView.showPlayerCards(bot);
                    mainController.updateAllDecksDisplay();
                    mainController.refreshGuide();
                    mainController.setWaitingNextTurn(false);

                    mainController.requestEndTurnFromBot();
                });
                secondCardTimer.setRepeats(false);
                secondCardTimer.start();
            } else {
                mainController.setWaitingNextTurn(false);
                //montre la main du joueur qui suit
                mainController.requestEndTurnFromBot();
            }
        });
        firstCardTimer.setRepeats(false);
        firstCardTimer.start();
    }

    /**
     * Le bot décide s'il pioche ou prend de la défausse
     */
    private void handleBotDrawDecision(BotModel bot) {
        mainController.setWaitingNextTurn(true);

        Timer decisionTimer = new Timer(800, e -> { // fluidité
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
                CardModel taken = gameManager.takeDiscard();
                gameBoardView.getBtnDefausse().setCardImage(gameBoardView.getCardImagePath(taken));
                gameBoardView.updateCardDisplay(
                        gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard(),
                        gameManager.getDeck().stackSize() > 0);
                System.out.println(bot.getName() + " prend la défausse : " + taken.getCardValue());
                mainController.refreshGuide();
                mainController.setWaitingNextTurn(false);

                //bot échange
                handleBotTurn();
            } else {
                //piocher
                CardModel drawn = gameManager.drawDeck();
                if (drawn.isFaceDown())
                    drawn.returnCard();
                gameBoardView.getBtnPioche().setCardImage(gameBoardView.getCardImagePath(drawn));
                gameBoardView.updateDiscardOnly(
                        gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard());
                System.out.println(bot.getName() + " pioche : " + drawn.getCardValue());
                mainController.refreshGuide();
                mainController.setWaitingNextTurn(false);

                //décide d'échanger ou défausser
                Timer showCardTimer = new Timer(1000, ev -> { // fluidité
                    handleBotTurn();
                });
                showCardTimer.setRepeats(false);
                showCardTimer.start();
            }
        });
        decisionTimer.setRepeats(false);
        decisionTimer.start();
    }

    /**
     * Le bot décide d'échanger ou de défausser après avoir pioché
     */
    private void handleBotAfterDraw(BotModel bot) {
        mainController.setWaitingNextTurn(true);

        Timer exchangeTimer = new Timer(600, e -> { // fluidité
            BotHandModel botHand = bot.getHand();
            BotStrategyInterface strategy = bot.getStrategy();
            CardModel drawCard = gameManager.getCurrentDrawCard();

            boolean exchange = strategy.ExchangeDrawCard(botHand, drawCard);

            if (exchange) {
                //échange après pioche
                int[] position = strategy.CardToExchange(botHand, drawCard);
                if (position[0] != -1 && position[1] != -1) {
                    gameManager.exchangeCardInHand(position[0], position[1]);
                    System.out.println(bot.getName() + " échange [" + position[1] + "," + position[0] + "]");

                    gameBoardView.showPlayerCards(bot);
                    gameBoardView.updateCardDisplay(
                            gameManager.getDiscardPile().getTopCard(),
                            gameManager.getDeck().stackSize() > 0);
                    mainController.refreshGuide();
                    mainController.setWaitingNextTurn(false);
                    mainController.requestEndTurnFromBot();
                }
            } else {
                //jeter la carte piochée puis révéler une carte
                gameManager.discardDrawCard();
                gameBoardView.updateCardDisplay(
                        gameManager.getDiscardPile().getTopCard(),
                        gameManager.getDeck().stackSize() > 0);
                System.out.println(bot.getName() + " défausse et va révéler une carte");
                mainController.refreshGuide();

                //revele une carte aléatoire de sa main
                Timer revealTimer = new Timer(800, ev -> { // fluidité
                    botHand.revealRandomCard(1);
                    
                    //regarde et supprimer les colonnes/lignes identiques
                    GameRuleModel.removeIdenticalColumns(botHand, gameManager.getDiscardPile());
                    GameRuleModel.removeIdenticalRows(botHand, gameManager.getDiscardPile());
                    
                    gameBoardView.showPlayerCards(bot);
                    System.out.println(bot.getName() + " révèle une carte");
                    mainController.setWaitingNextTurn(false);
                    mainController.requestEndTurnFromBot();
                });
                revealTimer.setRepeats(false);
                revealTimer.start();
            }
        });
        exchangeTimer.setRepeats(false);
        exchangeTimer.start();
    }

    /**
     * Le bot échange une carte après avoir pris de la défausse
     */
    private void handleBotAfterTakeDiscard(BotModel bot) {
        mainController.setWaitingNextTurn(true);

        Timer exchangeTimer = new Timer(800, e -> {
            BotHandModel botHand = bot.getHand();
            BotStrategyInterface strategy = bot.getStrategy();
            CardModel drawCard = gameManager.getCurrentDrawCard();

            //doit échanger une carte
            int[] position = strategy.CardToExchange(botHand, drawCard);
            if (position[0] != -1 && position[1] != -1) {
                gameManager.exchangeCardInHand(position[0], position[1]);
                System.out.println(bot.getName() + " échange [" + position[1] + "," + position[0] + "]");

                gameBoardView.showPlayerCards(bot);
                gameBoardView.updateCardDisplay(
                        gameManager.getDiscardPile().getTopCard(),
                        gameManager.getDeck().stackSize() > 0);
                mainController.refreshGuide();
            }
            mainController.setWaitingNextTurn(false);
            mainController.requestEndTurnFromBot();
        });
        exchangeTimer.setRepeats(false);
        exchangeTimer.start();
    }
}
