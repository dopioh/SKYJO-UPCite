package fr.upc.cr1a.model;

import fr.upc.cr1a.model.interfaces.BotStrategyInterface;
import java.util.HashMap;
import java.util.Map;

/**
 * bot qui compte le nombre de cartes joué et calcule la proba des cartes qui peuvent etre pioché
 * 
 */
public class BotCounting implements BotStrategyInterface {
    
    private GameManager gameManager;
    
    //carte dans le deck
    private static final Map<Integer, Integer> cardDeck = new HashMap<>();
    
    static { //permet d'initialiser avec un static
        //les cartes dans le deck
        cardDeck.put(-2, 5);
        cardDeck.put(-1, 10);
        cardDeck.put(0, 15);
        for (int i = 1; i <= 12; i++) {
            cardDeck.put(i, 10);
        }
    }
    
    /**
     * constructeur par défaut 
     */
    public BotCounting() {
        this.gameManager = null;
    }
    
    /**
     * constructeur avec GameManager
     * @param gameManager le gestionnaire de jeu
     */
    public BotCounting(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    /**
     * définir le GameManager
     * @param gameManager
     */
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public int getSeuil() {
        // calcule le seuil dynamiquement 
        return calculateAverage();
        
    }
    
    /**
     * compte les cartes visibles des joueurs et la défausse
     * @return une map avec le nbre de cartes de chaque valeur
     */
    private Map<Integer, Integer> countVisibleCards() {
        Map<Integer, Integer> visibleCards = new HashMap<>();
        
        //init le compteur
        for (int i = -2; i <= 12; i++) {
            visibleCards.put(i, 0);
        }
        
        // compter les cartes dans les mains des joueurs
        if (gameManager != null && gameManager.getPlayers() != null) {
            for (PlayerModel player : gameManager.getPlayers()) {
                PlayerHandModel hand = player.getHand();
                if (hand != null) {
                    //boucle
                    for (int row = 0; row < hand.getRows(); row++) {
                        for (int col = 0; col < hand.getColumns(); col++) {
                            CardModel card = hand.getCard(row, col);
                            //compter que les cartes visibles
                            if (card != null && !card.isFaceDown()) {
                                int value = card.getCardValue();
                                visibleCards.put(value, visibleCards.getOrDefault(value, 0) + 1);
                            }
                        }
                    }
                }
            }
            
            //compter les cartes dans la défausse
            DefausseModel defausse = gameManager.getDiscardPile();
            if (defausse != null && !defausse.isEmpty()) {
                //cpt tts les cartes de la défausse
                for (int i = 0; i < defausse.stackSize(); i++) {
                    CardModel card = defausse.getCardAt(i);
                    if (card != null) {
                        int value = card.getCardValue();
                        visibleCards.put(value, visibleCards.getOrDefault(value, 0) + 1);
                    }
                }
            }
        }
        
        return visibleCards;
    }
    
    /**
     * calcule le nbre possible de cartes restante dans la pioche
     * @return une map avec le nombre estimer de carte
     */
    private Map<Integer, Integer> RemainingCards() {
        Map<Integer, Integer> remainingCards = new HashMap<>();
        Map<Integer, Integer> visibleCards = countVisibleCards();
        
        for (int value = -2; value <= 12; value++) {
            int total = cardDeck.getOrDefault(value, 0);
            int visible = visibleCards.getOrDefault(value, 0);
            remainingCards.put(value, Math.max(0, total - visible));
        }
        
        return remainingCards;
    }
    




    /**
     * calcule la proba de piocher la carte
     * @param cardValue la valeur de la carte en entré
     * @return la probabilité 
     */
    private double calculateProbaOfTheCard(int cardValue) {
        Map<Integer, Integer> visibleCards = countVisibleCards();
        
        // Nombre RÉEL de cartes dans la pioche (pas une estimation)
        int cardsInDeck = 0;
        if (gameManager != null && gameManager.getDeck() != null) {
            cardsInDeck = gameManager.getDeck().stackSize();
        }
        
        if (cardsInDeck == 0) {
            return 0.0; // Pioche vide
        }
        
        // Combien de cartes de cette valeur ont été vues ?
        int visibleCount = visibleCards.getOrDefault(cardValue, 0);
        
        // Combien il en reste au total (total initial - visibles)
        int totalInitial = cardDeck.getOrDefault(cardValue, 0);
        int remainingTotal = totalInitial - visibleCount;
        
        // Ces cartes restantes sont soit dans la pioche, soit cachées dans les mains
        // On fait l'hypothèse qu'elles sont réparties proportionnellement
        // Probabilité estimée = cartes_restantes_de_cette_valeur / total_cartes_pioche
        
        return (double) remainingTotal / cardsInDeck;
    }
    
    /**
     * calcule la valeur moyenne des cartes restantes dans la pioche
     * @return la valeur moyenne
     */
    private int calculateAverage() {
        Map<Integer, Integer> remaining = RemainingCards();
        
        int totalRemaining = 0;
        int sumValues = 0;
        
        for (Map.Entry<Integer, Integer> entry : remaining.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();
            totalRemaining += count;
            sumValues += value * count;
        }
        
        if (totalRemaining == 0) {
            return 0;
        }
        
        return sumValues / totalRemaining;
    }
    


    @Override
    public boolean TakeFromDiscard(BotHandModel hand, CardModel discardCard) {
        if (discardCard == null) return false;
        
        int cardValue = discardCard.getCardValue();
        
        // tjrs prendre les cartes négatives
        if (cardValue <= 0) {
            return true;
        }

        //calcule de repiocher la carte
        double probSameCard = calculateProbaOfTheCard(cardValue);
        //si on a déjà la même carte en main
        int[] sameCard = hand.sameCardInHandPostition(discardCard);
        if (sameCard[0] != -1) {
            //prendre si probabilité est bonne
            if (probSameCard >= 0.65) {
                return true;
            }
            //sinon, prendre seulement si la carte est bonne 
            return cardValue <= getSeuil();
        }
        
        //si la carte est bonne, on la prend
        if (cardValue <= 2) {
            return true;
        }
        
        //calculer la valeur moyenne des cartes restantes
        int avgRemaining = calculateAverage();
        
        //prendre si mieux que la moyenne
        return cardValue < avgRemaining || cardValue <= 2;
    }
    
    @Override
    public boolean ExchangeDrawCard(BotHandModel hand, CardModel drawCard) {
        if (drawCard == null) return false;
        
        int cardValue = drawCard.getCardValue();
        
        //toujours échanger les cartes négatives
        if (cardValue <= 0) {
            return true;
        }
        
        //calculer la probabilité de repiocher cette même carte
        double probSameCard = calculateProbaOfTheCard(cardValue);
        
        //si on a déjà la même carte en main
        int[] sameCard = hand.sameCardInHandPostition(drawCard);
        if (sameCard[0] != -1) {
            //echanger si probabilité est bonne
            if (probSameCard >= 0.65) {
                return true;
            }
            //sinon, échanger seulement si la carte est en dessus du seuil
            return cardValue <= getSeuil();
        }
        
        //si très bonne carte échanger
        if (cardValue <= 2) {
            return true;
        }
        
        //si carte acceptable selon le seuil
        if (cardValue <= getSeuil()) {
            return true;
        }
        
        //si carte moyenne et probabilité forte alors on peut la garder
        if (cardValue <= 6 && probSameCard >= 0.15) {
            return true;
        }
        
        //défausser
        return false;
    }
    
    @Override
    public int[] CardToExchange(BotHandModel hand, CardModel card) {
        int[] sameCardPosition = hand.sameCardInHandPostition(card);
        
        //si on a la même carte en main
        if (sameCardPosition[0] != -1) {
            int actualCol = sameCardPosition[0];
            int sameCardRow = sameCardPosition[1];
            
            //calculer la proba de revoir cette carte
            double probSameCard = calculateProbaOfTheCard(card.getCardValue());
            
            //bonne proba jouer la colonne
            if (probSameCard >= 0.65) {
                //prio révéler les carte carché
                for (int i = 0; i < hand.getRows(); i++) {
                    CardModel cardInCol = hand.getCard(i, actualCol);
                    if (cardInCol == null) continue;
                    if (i == sameCardRow) continue;
                    if (cardInCol.isFaceDown()) {
                        return new int[]{actualCol, i};
                    }
                }
                
                //échanger la carte la plus haute de la colonne
                int maxRow = -1;
                int maxValue = Integer.MIN_VALUE;
                for (int i = 0; i < hand.getRows(); i++) {
                    CardModel cardInCol = hand.getCard(i, actualCol);
                    if (cardInCol == null) continue;
                    if (i == sameCardRow) continue;
                    if (!cardInCol.isFaceDown() && cardInCol.getCardValue() > maxValue) {
                        maxValue = cardInCol.getCardValue();
                        maxRow = i;
                    }
                }
                if (maxRow != -1) {
                    return new int[]{actualCol, maxRow};
                }
            }
        }
        
        //révéler une carte caché de la colonne si la proba d'avoir la meme carte est bonne
        for (int col = 0; col < hand.getColumns(); col++) {
            for (int row = 0; row < hand.getRows(); row++) {
                CardModel cardAt = hand.getCard(row, col);
                if (cardAt == null) continue;
                if (cardAt.isFaceDown()) {
                    return new int[]{col, row};
                }
            }
        }
        
        //si toutes les cartes sont révélées, échanger la carte la plus haute
        int maxCol = -1;
        int maxRow = -1;
        int maxValue = Integer.MIN_VALUE;
        
        for (int col = 0; col < hand.getColumns(); col++) {
            for (int row = 0; row < hand.getRows(); row++) {
                CardModel cardAt = hand.getCard(row, col);
                if (cardAt != null && !cardAt.isFaceDown()) {
                    if (cardAt.getCardValue() > maxValue) {
                        maxValue = cardAt.getCardValue();
                        maxCol = col;
                        maxRow = row;
                    }
                }
            }
        }
        
        if (maxCol != -1 && maxRow != -1) {
            return new int[]{maxCol, maxRow};
        }
        
        return new int[]{-1, -1};
    }
}
