package fr.dev.sensei.guild.keeper.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dev.sensei.guild.keeper.GuildKeeperModule;
import fr.dev.sensei.guild.keeper.GuildKeeperServices;
import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import io.javalin.Javalin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Base des tests d'API : un {@link GuildKeeperModule} cable sur une base SQLite
 * neuve (donc avec le catalogue de quetes seede) par methode de test.
 */
abstract class HttpApiTest {

    static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    GuildKeeperModule module;

    @BeforeEach
    void createModule() {
        module = GuildKeeperModule.usingDatabase(tempDir.resolve("guildkeeper.db"));
    }

    Javalin app() {
        return GuildKeeperServer.create(module);
    }

    /** Prepare des donnees avant un test (insertion de membres, d'attributions, ...). */
    void seed(Consumer<GuildKeeperServices> work) {
        module.run(work);
    }

    /** Cree une attribution de quete pour un membre deja enregistre. */
    static void assign(GuildKeeperServices services, String memberId, String questId, QuestAssignmentStatus status) {
        Member member = services.members().findById(memberId).orElseThrow();
        Quest quest = services.quests().findById(questId).orElseThrow();
        services.questAssignments().add(new QuestAssignment(member, quest, status));
    }
}
