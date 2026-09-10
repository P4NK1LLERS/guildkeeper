package fr.dev.sensei.guild.keeper.cucumber;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Squelette des steps de {@code features/rewards.feature} — ATELIER CHAPITRE 6.
 *
 * <p>Modèle : {@link RecruitmentSteps}. Câbler {@code RewardsDistributionService}
 * (avec {@code fr.dev.sensei.guild.keeper.rewards.FakeNotificationPort},
 * {@code ExperienceCalculator}, {@code LootCalculator}, un {@code InMemoryMemberRepository})
 * puis implémenter chaque méthode. Enfin, élargir {@code @SelectClasspathResource}
 * de {@link fr.dev.sensei.guild.keeper.RunCucumberTest} au dossier {@code "features"}.
 *
 * <p>Tant que ce n'est pas fait, {@code rewards.feature} n'est pas exécuté et ces
 * méthodes ne sont jamais appelées.
 */
public class RewardsSteps {

    @Soit("un aventurier {string} de rang {string}")
    public void un_aventurier_de_rang(String nom, String rang) {
        fail("Step à compléter (atelier chapitre 6)");
    }

    @Et("une quête {string} de difficulté {string} rapportant {int} d'expérience et {int} d'or")
    public void une_quete_de_difficulte(String titre, String difficulte, int experienceBase, int orBase) {
        fail("Step à compléter (atelier chapitre 6)");
    }

    @Quand("{string} termine la quête {string}")
    public void termine_la_quete(String nom, String titre) {
        fail("Step à compléter (atelier chapitre 6)");
    }

    @Alors("{string} gagne {int} points d'expérience")
    public void gagne_points_d_experience(String nom, int experienceAttendue) {
        fail("Step à compléter (atelier chapitre 6)");
    }

    @Et("{string} reçoit {int} pièces d'or de butin")
    public void recoit_pieces_d_or(String nom, int butinAttendu) {
        fail("Step à compléter (atelier chapitre 6)");
    }

    @Et("{string} est notifié")
    public void est_notifie(String nom) {
        fail("Step à compléter (atelier chapitre 6)");
    }
}
