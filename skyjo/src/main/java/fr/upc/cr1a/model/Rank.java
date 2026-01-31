package fr.upc.cr1a.model;

/**
 * Classe Enum qui gère les rank pré-defini
 */
public enum Rank {
    // Quels Rang ? Iron,Bonze, Silver, Gold, Platine,Emeraud, Diamand, Master, Challenger ? Ou plus ou moins ?
    // 100 elo entre chaque rank mais on peut augmenter ou baisser plus tard
    IRON(0,99),
    BRONZE(100,199),
    SILVER(200,299),
    GOLD(300,399),
    PLATINIUM(400,499),
    EMERALD(500,599),
    DIAMOND(600,699),
    MASTER(700,799),
    GRANDMASTER(800,899),
    CHALLENGER(900,10000);

    private final int eloMin;
    private final int eloMax;

    private Rank(int eloMin, int eloMax){
        this.eloMin = eloMin;
        this.eloMax = eloMax;
    }

    public static Rank findRankByElo(int elo){
        for (Rank r : values()){ //values() contient toutes les enum de Rank
            if (elo >= r.eloMin && elo <= r.eloMax){
                return r;
            }
        }
        return null; // ou throw une erreur.. à voir plus tard
    }
}
