package fr.upc.cr1a.model;
import java.util.Scanner;

/*
* Modèle de la main d'un joueur dans le jeu Skyjo
* Gère une grille de cartes 4x3 avec des fonctionnalités des règles du jeu
* @author Billy
 */

public class PlayerHandModel {

    private CardModel[][] grid; // Grille 3 lignes × 4 colonnes
    private final int rows = 3; //lignes
    private final int columns = 4; //colonnes
    
    //constructeur
    public PlayerHandModel(){
        grid = new CardModel[rows][columns];
    }
    
    public void placeCard(int row, int col, CardModel card){
        if (isAvailable(row, col)){
            grid[row][col] = card;
        }
    }

    public CardModel getCard(int row, int col){
        return grid[row][col];
    }
    public int getRows(){
        return rows;
    }
    public int getColumns(){
        return columns;
    }

    public void replaceCard(int row, int col, CardModel newCard){
        grid[row][col] = newCard;
    }

    public void revealCard(int row, int col){
        if (grid[row][col] != null){
            grid[row][col].revealCard(true);
        }
    }
    
    // regarde si toutes les cartes sont révélées
    public boolean allCardsRevealed(){
        for (CardModel[] x : grid){
            for(CardModel y : x){
                if (!y.isFaceDown()){
                    return false;
                }
            }
        }
        return true;
    }   

    //regarde le nombre de cartes révélées
    public int countRevealedCards(){
        int count = 0;
        for (CardModel[] x : grid){
            for(CardModel y : x){
                if (y.isFaceDown()){
                    count++;
                }
            }
        }
        return count;
    }

    /*
    * vérifie les colonnes pour 3 cartes identiques révélées
    * @return la colonne avec 3 cartes pareils sinon -1
    */
    public int checkColumns(){
        int res = -1;
        int i = 0;
        //boucle qui regarde si les cartes sont révélées et ont les meme valeurs
        for (int j = 0; j< columns;j++){
            if (grid[i][j].isFaceDown() && grid[i+1][j].isFaceDown() && grid[i+2][j].isFaceDown()){
                if (grid[i][j] == grid[i+1][j] && grid[i+1][j] == grid[i+2][j]) {
                    res = j;
                    return res;
                }
            }
        }
        return res;
    }
    
    //met des null à la colonne qui a 3 cartes identiques révélées
    public void removeColumn(int columnIndex){
        if (columnIndex == -1){
            return;
        }
        if (columnIndex < 4 && columnIndex > 0) {
            for (int i = 0; i <3 ;i++){
                grid[i][columnIndex] = null;
            }    
        }   
    }

    /*
    * vérifie les lignes pour 3 cartes identiques révélées
    * @return la ligne avec 3 cartes pareils sinon -1
    */
    public int checkRows(){
        int res = -1;
        int i = 0;
        //boucle qui regarde si les cartes sont révélées et ont les meme valeurs
        for (int j = 0; j< rows;j++){
            if (grid[j][i].isFaceDown() && grid[j][i+1].isFaceDown() && grid[j][i+2].isFaceDown() && grid[j][i+3].isFaceDown()){
                if (grid[j][i] == grid[j][i+1] && grid[j][i+1] == grid[j][i+2] && grid[j][i+2] == grid[j][i+3]){
                    res = j;
                    return res;
                }
            }
        }
        return res;
    }
    
    //met des null à la ligne qui a 4 cartes identiques révélées
    public void removeRow(int rowIndex){
        if (rowIndex == -1){
            return;
        }
        if (rowIndex < rows && rowIndex > 0) {
            for (int i = 0; i <columns ;i++){
                grid[rowIndex][i] = null;
            }    
        }   
    }

    //calcule le score final de la main du joueur
    public int calculateScore(){
        int score = 0;
        for (CardModel[] x : grid){
            for(CardModel y : x){
                if (y != null){
                    score += y.getCardValue();
                }
            }
        }
        return score;
    }
    

    public boolean isAvailable(int row, int col){
        return grid[row][col] == null;
    }

    public void revealCardsTemp(int count){
        Scanner scanner = new Scanner(System.in);

        for (int i = 0; i < count; i++){
            System.out.println("Entrer la ligne puis la colonne " + (i+1) + " (format: ligne colonne): ");
            int row = scanner.nextInt();
            int col = scanner.nextInt();
            if (isAvailable(row, col) && !grid[row][col].isFaceDown()){
                revealCard(col, row);
                System.out.println("carte révélée");
            } else {
                System.out.println("Position déja révélée");
                i--; 
            }
        }
        scanner.close();

    }

}