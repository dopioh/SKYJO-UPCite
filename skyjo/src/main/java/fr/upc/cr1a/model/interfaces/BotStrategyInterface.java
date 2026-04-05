package fr.upc.cr1a.model.interfaces;

import fr.upc.cr1a.model.BotHandModel;
import fr.upc.cr1a.model.CardModel;

/**
 * Interface pour la stratégie d'un bot
 * permet de créer différents types de bots avec des stratégies variées
 * 
 * @author Billy
 */
public interface BotStrategyInterface {
    
    /**
     * regarde si le bot doit prendre la carte de la défausse
     * @param hand la main du bot
     * @param discardCard la carte sur la défausse
     * @return true si le bot prend, false sinon
     */
    boolean TakeFromDiscard(BotHandModel hand, CardModel discardCard);
    
    /**
     * regarde si le bot doit échanger la carte piochée ou la défausser
     * @param hand la main du bot
     * @param drawCard la carte piochée
     * @return true si échange, false si défausse
     */
    boolean ExchangeDrawCard(BotHandModel hand, CardModel drawCard);
    
    /**
     * regarde quelle carte remplacer dans la main
     * @param hand la main du bot
     * @param card la carte à placer
     * @return tableau [row, col] de la position à remplacer
     */
    int[] CardToExchange(BotHandModel hand, CardModel card);
    
    /**
     * Retourne le seuil de valeur pour accepter une carte
     * @return le seuil (par exemple 5 pour une stratégie équilibrée)
     */
    int getSeuil();

    /**
     * décide si le bot doit jouer une carte action
     * @return true si le bot doit jouer une action
     */
    boolean shouldPlayAction();

}
