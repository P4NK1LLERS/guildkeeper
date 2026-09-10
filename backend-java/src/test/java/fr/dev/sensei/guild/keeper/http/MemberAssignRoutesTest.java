package fr.dev.sensei.guild.keeper.http;

import com.fasterxml.jackson.databind.JsonNode;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MemberAssignRoutesTest extends HttpApiTest {

    @Test
    void assigns_a_quest_to_a_free_member() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests", Map.of("questId", "1"));

            // Assert
            assertThat(response.code()).isEqualTo(201);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("questId").asText()).isEqualTo("1");
            assertThat(body.get("questTitle").asText()).isEqualTo("Nettoyer les caves de la guilde");
            assertThat(body.get("status").asText()).isEqualTo("ASSIGNED");
        });
    }

    @Test
    void rejects_assigning_when_the_member_already_has_a_quest_in_progress() {
        // Arrange
        seed(services -> {
            services.members().save(Member.novice("m-dragan", "Dragan", 3));
            assign(services, "m-dragan", "1", QuestAssignmentStatus.ASSIGNED);
        });

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests", Map.of("questId", "1"));

            // Assert
            assertThat(response.code()).isEqualTo(409);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("error").asText()).isEqualTo("CONFLICT");
            assertThat(body.get("message").asText()).contains("en cours");
        });
    }

    @Test
    void rejects_assigning_when_the_prerequisite_is_not_completed() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests", Map.of("questId", "2"));

            // Assert
            assertThat(response.code()).isEqualTo(409);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("error").asText()).isEqualTo("CONFLICT");
            assertThat(body.get("message").asText()).contains("Nettoyer les caves de la guilde");
        });
    }

    @Test
    void assigns_a_quest_once_its_prerequisite_is_completed() {
        // Arrange
        seed(services -> {
            services.members().save(Member.novice("m-dragan", "Dragan", 3));
            assign(services, "m-dragan", "1", QuestAssignmentStatus.COMPLETED);
        });

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests", Map.of("questId", "2"));

            // Assert
            assertThat(response.code()).isEqualTo(201);
            assertThat(MAPPER.readTree(response.body().string()).get("questTitle").asText())
                    .isEqualTo("Escorter la caravane marchande");
        });
    }

    @Test
    void returns_404_when_assigning_to_an_unknown_member() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Ghost/quests", Map.of("questId", "1"));

            // Assert
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void returns_404_when_assigning_an_unknown_quest() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests", Map.of("questId", "999"));

            // Assert
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void rejects_an_assignment_without_a_quest_id() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(400);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("VALIDATION");
        });
    }
}
