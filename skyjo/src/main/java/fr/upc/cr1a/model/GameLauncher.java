package fr.upc.cr1a.model;

import fr.upc.cr1a.view.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe pour lancer le jeu 
 * Elle doit servir que pour ca et pas pour autre chose
 */
public class GameLauncher {
    /*
    public GameLauncher(){
        // on lance la frame ici 
        new Frame();
    }
     */
    
    private GameManager gameManager;
    private Scanner scanner;
    
    public GameLauncher() {
        scanner = new Scanner(System.in);
        startGame();
    }
    
    /**
     * Démarre le jeu et gère le menu principal
     */
    private void startGame() {
        List<String> playerNames = new ArrayList<>();
        //demande le nombre de joueurs
        int nbJoueurs;
        do {
            System.out.print("Nombre de joueurs (2-8) : ");
            nbJoueurs = scanner.nextInt();
            scanner.nextLine();
            
            if (nbJoueurs < 2 || nbJoueurs > 8) {
                System.out.println("Le nombre de joueurs doit être entre 2 et 8 !");
            }
        } while (nbJoueurs < 2 || nbJoueurs > 8);
        
        //Donnez le nom des joueurs
        for (int i = 1; i <= nbJoueurs; i++) {
            System.out.print("Nom du joueur " + i + " : ");
            playerNames.add(scanner.nextLine());
        }
        System.out.println("------------------------------------------------------------------");
        
        //créer le game manager
        gameManager = new GameManager(playerNames);
        
        // Lancer la partie
        playGame();
    }
    
    /**
     * déroulement de la partie
     */
    private void playGame() {
        //init la première manche
        gameManager.initializeGame();
        
        //choisir 2 cartes pour chaque joueur au début
        for (PlayerModel player : gameManager.getPlayers()) {
            System.out.println(player.getName() + " choisissez 2 cartes à révéler \n");
            showHand(player, false);
            
            for (int i = 0; i < 2; i++) {
                int row, col;
                //choisir la carte jusuqu'a donner une carte valide
                do {
                    System.out.print("Carte " + (i + 1) + " - Ligne (0-2) : ");
                    row = scanner.nextInt();
                    System.out.print("Carte " + (i + 1) + " - Colonne (0-3) : ");
                    col = scanner.nextInt();
                    
                    if (row < 0 || row >= 3 || col < 0 || col >= 4) {
                        System.out.println("\nPosition invalide ! Réessayez.\n");
                    } else if (player.getHand().getCard(row, col).isFaceDown() == false) {
                        System.out.println("\nCette carte est déjà révélée !\n");
                    } else {
                        break;
                    }
                } while (true);
                
                player.getHand().revealCard(row, col);
                System.out.println("\nCarte révélée : " + player.getHand().getCard(row, col).getCardValue());
                showHand(player, false);
                System.out.println("------------------------------------------------------------------");
            }
            scanner.nextLine(); 
        }
        
        System.out.println("\nCarte sur la défausse : " + gameManager.getDiscardPile().getTopCard().getCardValue() + "\n");
        
        //boucle de la partie (A bouger plus tard dans la gameLoop peut etre jsp faut ce concerté les amis)
        while (gameManager.getGameState() == GameManager.GameState.IS_PLAYING || gameManager.getGameState() == GameManager.GameState.LAST_ROUND) {
            playTurn();
        }
        
        //fin de partie
        if (gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
            System.out.println("\nPartie terminée ");
        }
        
        scanner.close();
    }
    
    /**
     *tour complet d'un joueur
     */
    private void playTurn() {
        //début du tour
        gameManager.startTurn();
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        
        System.out.println("----------------------------------------------------------------");
        
        //affiche la main du joueur
        showHand(currentPlayer, false);
        
        //montre la carte sur la défausse
        System.out.println("\nCarte sur la défausse : " + gameManager.getDiscardPile().getTopCard().getCardValue());
        
        //choisir piocher ou prendre de la défausse
        System.out.println("----------------------------------------------------------------");
        System.out.println("\n Action possible :\n");
        System.out.println("1 = Piocher");
        System.out.println("2 = Prendre de la défausse");
        System.out.print("\nVotre choix : ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        CardModel drawnCard;
        boolean mustExchange = false;
        
        //choix de prendre de la défausse
        if (choice == 2) {
            //échanger une carte
            drawnCard = gameManager.takeDiscard();
            mustExchange = true;
            System.out.println("----------------------------------------------------");
            System.out.println("Vous avez pris : " + drawnCard.getCardValue());
        } else { //piocher une carte
            drawnCard = gameManager.drawDeck();
            System.out.println("----------------------------------------------------");
            System.out.println("Vous avez pioché : " + drawnCard.getCardValue());
        }
    
        //échanger
        if (mustExchange) {
            System.out.println("\nEchanger la carte avec une carte de votre main");
            exchangeCard(currentPlayer);
        //quand le jouer pioche 2 actions possibles
        } else {
            System.out.println("\n Action possible :\n");
            System.out.println("1 = Echanger");
            System.out.println("2 = défausser puis révéler une carte");
            System.out.print("\nVotre choix : ");
            
            int action = scanner.nextInt();
            scanner.nextLine();
            //échanger
            if (action == 1) {
                exchangeCard(currentPlayer);
            } else { //défausser puis révéler une carte
                //défausser
                gameManager.discardDrawCard();
                System.out.println("\nCarte défaussée");
                //reveler une carte de la main
                System.out.println("Revéler une carte de votre main\n");
                System.out.println("----------------------------------------------------------------");
                showHand(currentPlayer, false);
                //choisit la carte à reveler
                int row, col;
                do {
                    System.out.print("\nLigne (0-2) : ");
                    row = scanner.nextInt();
                    System.out.print("Colonne (0-3) : ");
                    col = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (row < 0 || row >= 3 || col < 0 || col >= 4) {
                        System.out.println("\nPosition invalide !\n");
                    } else if (currentPlayer.getHand().getCard(row, col) == null) {
                        System.out.println("\nPas de carte à cette position !\n");
                    } else if (!currentPlayer.getHand().getCard(row, col).isFaceDown()) {
                        System.out.println("\nCette carte est déjà révélée !\n");
                    } else {
                        break;
                    }
                } while (true);
                //revéler la carte
                currentPlayer.getHand().revealCard(row, col);
                System.out.println("--------------------------------------------------------------");
                System.out.println("\nCarte révélée : " + currentPlayer.getHand().getCard(row, col).getCardValue());
            }
        }
        
        //afficher la main
        showHand(currentPlayer, false);
        
        //passe au joueur
        System.out.println("\nAppuyez sur Entrée pour continuer :)");
        scanner.nextLine();
        
        gameManager.nextPlayer();
    }
    
    /**
     *fonction pour échanger une carte
     */
    private void exchangeCard(PlayerModel player) {
        System.out.println("\nDonnez l'emplacement de la carte à échanger");
        System.out.println("------------------------------------------------------------");
        showHand(player, false);
        
        int row, col;
        do {
            System.out.print("\nLigne (0-2) : ");
            row = scanner.nextInt();
            System.out.print("Colonne (0-3) : ");
            col = scanner.nextInt();
            scanner.nextLine();
            
            if (row < 0 || row >= 3 || col < 0 || col >= 4) {
                System.out.println("\nPosition invalide !\n");
            } else if (player.getHand().getCard(row, col) == null) {
                System.out.println("\nPas de carte à cette position !\n");
            } else {
                break;
            }
        } while (true);
        
        CardModel oldCard = player.getHand().getCard(row, col);
        gameManager.exchangeCardInHand(col, row);
        System.out.println("\nCarte échangé. Carte défaussée : " + oldCard.getCardValue() + "\n");
        System.out.println("-------------------------------------------------------------------------");
    }
    
    /**
     *affiche la main d'un joueur
     *si la carte est null = X; face cachée = ?; face visible = valeur
     */
    private void showHand(PlayerModel player, boolean showValues) {
        System.out.println("Main de " + player.getName() + "\n");
        System.out.println("         Col 0   Col 1   Col 2   Col 3");
        //boucle qui affiche la main
        for (int row = 0; row < 3; row++) {
            System.out.print("Ligne " + row + " ");
            for (int col = 0; col < 4; col++) {
                CardModel card = player.getHand().getCard(row, col);
                if (card == null) {
                    System.out.print("[  X  ]");
                } else if (card.isFaceDown()) {
                    System.out.print("[  ?  ]");
                } else {
                    System.out.printf("[ %3d ]", card.getCardValue());
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    
    }
    public static void main(String[] args) {
        new GameLauncher();
    }
}
