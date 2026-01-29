package fr.upc.cr1a.model;
import java.util.Scanner;

import java.sql.RowId;
import java.util.ArrayList;

/*
* Modèle de la main d'un joueur dans le jeu Skyjo
* Gère une grille de cartes 4x3 avec des fonctionnalités des règles du jeu
* @author Billy
 */

public class PlayerHandModel {

    private Card[][] grid; // Grille 3 lignes × 4 colonnes
    private static final int ROWS = 3; //lignes
    private static final int COLUMNS = 4; //colonnes
    
    //constructeur
    public PlayerHandModel(){
        grid = new Card[ROWS][COLUMNS];
    }
    
    public void placeCard(int row, int col, Card card){
        if (isAvailable(row, col)){
            grid[row][col] = card;
        }
    }

    public Card getCard(int row, int col){
        return grid[row][col];
    }

    public void replaceCard(int row, int col, Card newCard){
        grid[row][col] = newCard;
    }

    public void revealCard(int row, int col){
        if (grid[row][col] != null){
            grid[row][col].CardReveal();
        }
    }
    
    // regarde si toutes les cartes sont révélées
    public boolean allCardsRevealed(){
        for (Card[] x : grid){
            for(Card y : x){
                if (!y.isRevealed()){
                    return false;
                }
            }
        }
        return true;
    }   

    //regarde le nombre de cartes révélées
    public int countRevealedCards(){
        int count = 0;
        for (Card[] x : grid){
            for(Card y : x){
                if (y.isRevealed()){
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
        for (int j = 0; j< COLUMNS;j++){
            if (grid[i][j].isRevealed() && grid[i+1][j].isRevealed() && grid[i+2][j].isRevealed()){
                if (grid[i][j].getValue() == grid[i+1][j].getValue() && grid[i+1][j].getValue() == grid[i+2][j].getValue()){
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
        for (int j = 0; j< ROWS;j++){
            if (grid[j][i].isRevealed() && grid[j][i+1].isRevealed() && grid[j][i+2].isRevealed() && grid[j][i+3].isRevealed()){
                if (grid[j][i].getValue() == grid[j][i+1].getValue() && grid[j][i+1].getValue() == grid[j][i+2].getValue() && grid[j][i+2].getValue() == grid[j][i+3].getValue()){
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
        if (rowIndex < 3 && rowIndex > 0) {
            for (int i = 0; i <4 ;i++){
                grid[rowIndex][i] = null;
            }    
        }   
    }

    //calcule le score final de la main du joueur
    public int calculateScore(){
        int score = 0;
        for (Card[] x : grid){
            for(Card y : x){
                if (y != null){
                    score += y.getValue();
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
            if (isAvailable(row, col) && !grid[row][col].isRevealed()){
                revealCard(col, row);
                System.out.println("carte révélée");
            } else {
                System.out.println("Position déja révélée");
                i--; 
            }
        }

    }
}