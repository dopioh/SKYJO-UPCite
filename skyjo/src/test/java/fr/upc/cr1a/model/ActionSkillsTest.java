package fr.upc.cr1a.model;

import org.junit.jupiter.api.Test;

import fr.upc.cr1a.model.player.PlayerData;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Couverture de ActionSkills par tests unitaires 
 * @author Nassim
 */
class ActionSkillsTest {

    @Test
    void testTradeCardWithPlayerEchange() {
        // init joueurs + mains
        ArrayList<PlayerModel> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            players.add(new PlayerModel(new PlayerData("J" + i)));
        }

        for (PlayerModel player : players) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 4; c++) {
                    NumberCardModel card = new NumberCardModel(false, r+c, "vert", false);
                    player.getHand().placeCard(r, c, card);
                }
            }
        }

        // joueur 0 échange sa carte (0,0) avec la carte (1,1) du joueur 1
        String input = "1\n0\n0\n1\n1\n"; // serie d'input clavier -> simule les input utilisateur
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int valeurJoueur0Avant = players.get(0).getHand().getCard(0, 0).getCardValue();
        int valeurJoueur1Avant = players.get(1).getHand().getCard(1, 1).getCardValue();

        ActionSkills.tradeCardWithPlayer(scanner, players, 0);

        assertEquals(valeurJoueur1Avant, players.get(0).getHand().getCard(0, 0).getCardValue());
        assertEquals(valeurJoueur0Avant, players.get(1).getHand().getCard(1, 1).getCardValue());
    }


    @Test
    void testReplaceCardInHandEchange() {
        // init joueurs + mains
        ArrayList<PlayerModel> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            players.add(new PlayerModel(new PlayerData("J" + i)));
        }

        for (PlayerModel player : players) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 4; c++) {
                    NumberCardModel card = new NumberCardModel(false, r+c, "vert", false);
                    player.getHand().placeCard(r, c, card);
                }
            }
        }

        // échange les cartes (0,0) et (2,3)
        String input = "0\n0\n2\n3\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int val1Avant = players.get(0).getHand().getCard(0, 0).getCardValue();
        int val2Avant = players.get(0).getHand().getCard(2, 3).getCardValue();

        ActionSkills.replaceCardInHand(scanner, players, 0);

        assertEquals(val2Avant, players.get(0).getHand().getCard(0, 0).getCardValue());
        assertEquals(val1Avant, players.get(0).getHand().getCard(2, 3).getCardValue());
    }


    @Test
    void testReplaceCardInHandAvecCaseVide() {
        // init joueurs + mains
        ArrayList<PlayerModel> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            players.add(new PlayerModel(new PlayerData("J" + i)));
        }

        for (PlayerModel player : players) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 4; c++) {
                    NumberCardModel card = new NumberCardModel(false, r+c, "vert", false);
                    player.getHand().placeCard(r, c, card);
                }
            }
        }
        // mettre une case vide
        players.get(0).getHand().replaceCard(0, 0, null); // init de la case vide
        String input = "0\n0\n2\n3\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int val2Avant = players.get(0).getHand().getCard(2, 3).getCardValue();
        ActionSkills.replaceCardInHand(scanner, players, 0);
        assertEquals(val2Avant, players.get(0).getHand().getCard(2, 3).getCardValue());
    }


    @Test
    void testPlayTwoTurn() {
        // init joueurs + mains
        ArrayList<PlayerModel> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            players.add(new PlayerModel(new PlayerData("J" + i)));
        }

        for (PlayerModel player : players) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 4; c++) {
                    NumberCardModel card = new NumberCardModel(false, r+c, "vert", false);
                    player.getHand().placeCard(r, c, card);
                }
            }
        }
        String input = "";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        GameManager gm = new GameManager();
        // on verif si aucune erreur n'est lancée
        assertDoesNotThrow(() -> ActionSkills.playTwoTurn(scanner, players, 0, gm));
    }


    @Test
    void testPlayLastActionInDefausse() {
        ArrayList<PlayerData> playersData = new ArrayList<>();
        playersData.add(new PlayerData("J1"));
        playersData.add(new PlayerData("J2"));
        GameManager gm = new GameManager(playersData, true);

        ArrayList<PlayerModel> gmPlayers = new ArrayList<>(gm.getPlayers());

        if (gm.getDiscardActionPile() != null && gm.getDiscardActionPile().stackSize() > 0) {
            String input = "";
            Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
            int actionHandSizeAvant = gmPlayers.get(0).getActionHand().size();
            ActionSkills.playLastActionInDefausse(scanner, gmPlayers, 0, gm);
            assertEquals(actionHandSizeAvant + 1, gmPlayers.get(0).getActionHand().size());
        }
    }


    @Test
    void testDrawThreeCard() {
        ArrayList<PlayerData> playersData = new ArrayList<>();
        playersData.add(new PlayerData("J1"));
        playersData.add(new PlayerData("J2"));
        GameManager gm = new GameManager(playersData, false);
        gm.initializeGame();

        ArrayList<PlayerModel> gmPlayers = new ArrayList<>(gm.getPlayers());
        // choix première carte position (0,0)
        String input = "1\n0\n0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> ActionSkills.drawThreeCard(scanner, gmPlayers, 0, gm));
    }


    @Test
    void testSpyOneColumn() {
        ArrayList<PlayerData> playersData = new ArrayList<>();
        playersData.add(new PlayerData("J1"));
        playersData.add(new PlayerData("J2"));
        GameManager gm = new GameManager(playersData, false);
        gm.initializeGame();

        ArrayList<PlayerModel> gmPlayers = new ArrayList<>(gm.getPlayers());
        // choix colonne 2
        String input = "2\n\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> ActionSkills.spyOneColumn(scanner, gmPlayers, 0, gm));
    }
}
