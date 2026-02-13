package fr.upc.cr1a.controller;

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
 * @author Mei
 * @author Ian
 */
public class GameBoardController {

    GameBoardModel gameBoardModel;
    GameBoardView gameBoardView;
    GameManager gameManager;
    private boolean waitingNextTurn = false; // empeche de cliquer pendant l'attente

    /**
     * Initialise la view et le Model du GameBoard
     */
    public GameBoardController(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameManager gameManager){
        this.gameBoardModel = gameBoardModel;
        this.gameBoardView = gameBoardView;
        this.gameManager = gameManager;

        listener();
    }
    
    /**
     * les listeners du plateau :
     * - bouton retour vers le menu
     * - clic sur les cartes affichées
     * 
     * Les boutons joueur a gauche ne changent plus la main affichée mais
     * la fonction buttonPlayerCalled existe toujours dans le model si
     * on a besoin de changer la main affichée a droite
     * 
     * IANNN MON LISTENER A GAUCHEEEEE 
     */
    public void listener(){

        JButton retour = gameBoardView.getBtnRetour();
        retour.addActionListener(e -> gameBoardModel.retourMenuCalled());

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
                    if (card == null || !card.isFaceDown()) return; // rien faire si deja revelé

                    // Reveler la carte
                    currentPlayer.getHand().revealCard(r, c);
                    gameBoardView.showPlayerCards(currentPlayer);

                    // Attendre 2 secondes et passer au joueur suivant
                    waitingNextTurn = true;
                    Timer timer = new Timer(2000, evt -> {
                        switchToNextPlayer();
                        waitingNextTurn = false;
                    });
                    timer.setRepeats(false);
                    timer.start();
                });
            }
        }

        int s = 1;
        for (JButton jb : gameBoardView.getListPlayerButton()){
            int tmp = s;
            jb.addActionListener(e -> gameBoardModel.buttonPlayerCalled(tmp));
            s++;
        }

    }

    /**
     * Passe au joueur suivant via GameManager et affiche ses cartes
     */
    private void switchToNextPlayer() {
        gameManager.nextPlayer();
        gameBoardView.showPlayerCards(gameManager.getCurrentPlayer());
    }
}
