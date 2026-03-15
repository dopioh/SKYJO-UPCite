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
    private enum ActionMode { NONE, SPY_ONE_COLUMN, REPLACE_CARD_FIRST, REPLACE_CARD_SECOND }
    private ActionMode actionMode = ActionMode.NONE;
    private int replaceFirstRow = -1;
    private int replaceFirstCol = -1;

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
     * Logique d'activation d'une carte action
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

    /**
     * Logique de la carte action une fois activée
     */
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
                CardModel card = player.getHand().getCard(row, col);
                if (card == null) return;
                replaceFirstRow = row;
                replaceFirstCol = col;
                actionMode = ActionMode.REPLACE_CARD_SECOND;
                gameBoardView.setActionGuide("SWAP : Clique sur la 2ème carte à échanger");
                break;
            
            case REPLACE_CARD_SECOND:
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

            default:
                break;
        }
    }

    /**
     * Passe au joueur suivant via GameManager et affiche ses cartes
     */
    public void switchToNextPlayer() {
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
