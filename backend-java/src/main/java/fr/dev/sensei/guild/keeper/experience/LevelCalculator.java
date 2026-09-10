package fr.dev.sensei.guild.keeper.experience;

/**
 * Calcul pur du niveau d'affichage d'un membre a partir de ses points d'experience.
 *
 * <p>Regles :
 * <ul>
 *   <li>0 XP -> niveau 1 ;</li>
 *   <li>+1 niveau tous les {@value #EXPERIENCE_PER_LEVEL} XP (100 -> 2, 250 -> 3, ...) ;</li>
 *   <li>une experience negative est refusee.</li>
 * </ul>
 *
 * <p>Classe d'affichage : elle n'est volontairement pas cablee au reste du domaine.
 */
public class LevelCalculator {

    /** Palier d'experience separant deux niveaux consecutifs. */
    static final int EXPERIENCE_PER_LEVEL = 100;

    /**
     * @param experiencePoints experience cumulee du membre, positive ou nulle
     * @return le niveau d'affichage, a partir de 1
     * @throws IllegalArgumentException si {@code experiencePoints} est negatif
     */
    public int levelFor(int experiencePoints) {
        if (experiencePoints < 0) {
            throw new IllegalArgumentException(
                    "Les points d'experience ne peuvent pas etre negatifs : " + experiencePoints);
        }
        return 1 + experiencePoints / EXPERIENCE_PER_LEVEL;
    }
}
