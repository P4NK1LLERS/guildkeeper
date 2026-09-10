package fr.dev.sensei.guild.keeper;

import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Jeu de donnees de demonstration, charge au demarrage du serveur uniquement si
 * {@code GUILDKEEPER_ENV=dev}, pour que le module TypeScript ait de quoi
 * travailler sans avoir a passer d'abord des requetes d'ecriture.
 *
 * <p>Idempotent : ne fait rien si la guilde a deja des membres.
 */
public final class DemoData {

    private static final Logger LOG = LoggerFactory.getLogger(DemoData.class);
    private static final String ENV_VAR = "GUILDKEEPER_ENV";

    private DemoData() {
    }

    public static void seedIfDevEnvironment(GuildKeeperModule module) {
        if (!"dev".equalsIgnoreCase(System.getenv(ENV_VAR))) {
            return;
        }
        boolean seeded = module.execute(services -> {
            if (!services.members().findAll().isEmpty()) {
                return false;
            }
            for (String name : List.of("Albéric", "Attila", "Dragan", "Dorian", "Dante", "Ektor")) {
                services.recruitment().recruit(name);
            }
            Member dragan = services.members().findByName("Dragan").orElseThrow();
            services.questAssignments().add(new QuestAssignment(
                    dragan, services.quests().findById("1").orElseThrow(), QuestAssignmentStatus.COMPLETED));
            services.questAssignments().add(new QuestAssignment(
                    dragan, services.quests().findById("2").orElseThrow(), QuestAssignmentStatus.ASSIGNED));
            return true;
        });
        LOG.info(seeded ? "Jeu de demo charge (GUILDKEEPER_ENV=dev)." : "Jeu de demo deja present, rien a faire.");
    }
}
