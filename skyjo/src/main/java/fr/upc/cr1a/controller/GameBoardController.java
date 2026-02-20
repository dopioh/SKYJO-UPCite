package fr.upc.cr1a.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.GameBoardModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.view.GameBoardView;

/**
 * Controller pour gerer les connections entre la view et le model
 * Du plateau de jeu
 * 
 * @see GameBoardView vue graphique
 * @see GameBoardModel logique du plateau
 * 
 * @author Mei,Ian,Hugo
 */
public class GameBoardController {

    GameBoardModel gameBoardModel;
    GameBoardView gameBoardView;
    GameManager gameManager;
    private boolean waitingNextTurn = false; // empeche de cliquer pendant l'attente
    private boolean waitingReveal = false;
    // pour fixer le clique du playerpreview
    private boolean isPinned = false;

    /**
     * Initialise la view et le Model du GameBoard
     */
    public GameBoardController(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameManager gameManager){
        this.gameBoardModel = gameBoardModel;
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;

        listener();
        refreshGuide();
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
    public void listener(){

        JButton retour = gameBoardView.getBtnRetour();
        retour.addActionListener(e -> gameBoardModel.retourMenuCalled());

        gameBoardView.getBtnPioche().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (waitingNextTurn) return;
            if (gameManager.getTurnPhase() != GameManager.TurnPhase.DRAW_DECISION) return;
            CardModel drawn = gameManager.drawDeck();
            if (drawn.isFaceDown()) drawn.returnCard();
            gameBoardView.getBtnPioche().setCardImage(gameBoardView.getCardImagePath(drawn));
            gameBoardView.updateDiscardOnly(
                gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard()
            );
            refreshGuide();
        });

        gameBoardView.getBtnDefausse().addActionListener(e -> {
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) return;
            if (waitingNextTurn) return;
            //Si on prend a partir de la défausse
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                if (gameManager.getDiscardPile().isEmpty()) return;
                CardModel taken = gameManager.takeDiscard();
                gameBoardView.getBtnDefausse().setCardImage(gameBoardView.getCardImagePath(taken));
                gameBoardView.updateCardDisplay(
                    gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard(),
                    gameManager.getDeck().stackSize() > 0
                );
                refreshGuide();
                return;
            }

            // Reveal de la carte du deck
            if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
                gameManager.discardDrawCard();
                waitingReveal = true; // maintenant il doit révéler une carte

                gameBoardView.updateCardDisplay(
                    gameManager.getDiscardPile().getTopCard(),
                    gameManager.getDeck().stackSize() > 0
                );
            }
            refreshGuide();
        });

        // listeners pour les cartes, retourner la carte au clic puis switch joueur après 2s
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                final int r = row;
                final int c = col;
                gameBoardView.getCardButtons()[row][col].addActionListener(e -> {
                    if (waitingNextTurn) return; // bloque les clics pendant l'attente
                    PlayerModel currentPlayer = gameBoardView.getCurrentPlayer();
                    if (currentPlayer == null) return;

                    CardModel card = currentPlayer.getHand().getCard(r, c);

                    //Partie Reveal de 2 cartes en debut de partie
                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.INIT_REVEAL) {
                        boolean ok = gameManager.revealInitCard(c, r); 
                        if (!ok) return;
                        gameBoardView.showPlayerCards(gameManager.getCurrentPlayer());
                        gameBoardView.updateCardDisplay(
                            gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard(),
                            gameManager.getDeck().stackSize() > 0
                        );
                        refreshGuide();
                        return;
                    }

                    // Reveler apres avoir défausser
                    if (waitingReveal) {
                        if (card == null || !card.isFaceDown()) return;

                        currentPlayer.getHand().revealCard(r, c);
                        gameBoardView.showPlayerCards(currentPlayer);

                        waitingReveal = false;
                        switchToNextPlayer();
                        refreshGuide();
                        return;
                    }

                    // Echange de carte pioche deck
                    if (gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK
                        || gameManager.getTurnPhase() == GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD) {

                        gameManager.exchangeCardInHand(c, r); // col, row (comme ton code)
                        gameBoardView.showPlayerCards(currentPlayer);

                        gameBoardView.updateCardDisplay(
                            gameManager.getDiscardPile().getTopCard(),
                            gameManager.getDeck().stackSize() > 0
                        );

                        switchToNextPlayer();
                        refreshGuide();
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

        int s = 0;
        for (JButton jb : gameBoardView.getListPlayerButton()){
            int tmp = s;
            jb.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e){
                    gameBoardModel.buttonPlayerCalled(tmp);
                    gameBoardView.showOtherPlayerMain(true);
                    isPinned = false;
                }

                @Override
                public void mouseClicked(MouseEvent e){
                    gameBoardModel.buttonPlayerCalled(tmp);
                    gameBoardView.showOtherPlayerMain(true);
                    isPinned = false;
                }

                @Override
                public void mouseExited( MouseEvent e) {
                    if (!isPinned){
                        gameBoardView.showOtherPlayerMain(false);
                        isPinned = true;
                    }
                }
                @Override
                public void mouseReleased( MouseEvent e) {
                    if (!isPinned){
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
    private void switchToNextPlayer() {
        gameManager.nextPlayer();
        gameManager.startTurn();
        gameBoardView.showPlayerCards(gameManager.getCurrentPlayer());
        gameBoardView.updateCardDisplay(
            gameManager.getDiscardPile().isEmpty() ? null : gameManager.getDiscardPile().getTopCard(),
            gameManager.getDeck().stackSize() > 0
        );
        refreshGuide();
    }

    /**
     * Message d'action indiqunat au joueur quoi faire
     */
    private void refreshGuide() {
        switch (gameManager.getTurnPhase()) {
            case DRAW_DECISION -> gameBoardView.setActionGuide("Choisis : Pioche ou Défausse");
            case CARD_DRAW_FROM_DECK -> gameBoardView.setActionGuide("Echange ou Défausse puis revele une carte");
            case CARD_TAKE_FROM_DISCARD -> gameBoardView.setActionGuide("Clique une carte de ta main pour échanger");
            case TURN_ENDED -> gameBoardView.setActionGuide("Tour terminé");
            case INIT_REVEAL -> gameBoardView.setActionGuide(gameManager.getCurrentPlayer().getName() + " doit réveler 2 cartes (" + gameManager.getInitRevealCount() + ")");
        }
    }
}
