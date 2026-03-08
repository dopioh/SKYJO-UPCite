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
    GameManager gameManager;
    private GameBoardBotController botController;
    
    private boolean waitingNextTurn = false; // empeche de cliquer pendant l'attente
    private boolean waitingReveal = false;
    // pour fixer le clique du playerpreview
    private boolean isPinned = false;
    private AudioManager audio;
    // panel de fin de manche
    RoundEndPanel currentRoundEndPanel = null;

    /**
     * Initialise la view et le Model du GameBoard
     */
    public GameBoardController(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameManager gameManager) {
        this.gameBoardModel = gameBoardModel;
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;
        this.audio = new AudioManager();
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
                    
                        botController.handleBotTurn();
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
     * Passe au joueur suivant via GameManager et affiche ses cartes
     */
    public void switchToNextPlayer() {
        waitingNextTurn = true;
        gameBoardView.hideCursorCard();
        gameManager.nextPlayer();
        // détecter fin de manche ou fin de partie
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

        if (gameManager.getCurrentPlayer() instanceof BotModel) {
            botController.handleBotTurn();
        } else {
            waitingNextTurn = false;
        }
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
