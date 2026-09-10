package fr.dev.sensei.guild.keeper.missions;

/**
 * Calcul pur de la valeur de butin d'une quete completee.
 *
 * <p>Formule : {@code baseLootValue + baseLootValue * luck / 20}, en arithmetique
 * entiere (les pieces d'or sont des entiers). La multiplication est effectuee
 * avant la division pour limiter la perte de precision.
 */
public class LootCalculator {

    public int calculateLoot(int baseLootValue, int luck) {
        if (baseLootValue < 0) {
            throw new IllegalArgumentException("Le butin de base ne peut pas etre negatif : " + baseLootValue);
        }
        if (luck < 1 || luck > 10) {
            throw new IllegalArgumentException("La chance doit etre comprise entre 1 et 10 : " + luck);
        }
        return baseLootValue + (baseLootValue * luck) / 20;
    }
}
