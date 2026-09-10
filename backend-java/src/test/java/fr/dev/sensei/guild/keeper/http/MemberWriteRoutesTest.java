package fr.dev.sensei.guild.keeper.http;

import com.fasterxml.jackson.databind.JsonNode;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MemberWriteRoutesTest extends HttpApiTest {

    @Test
    void recruits_a_new_member() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members", Map.of("name", "Dragan"));

            // Assert
            assertThat(response.code()).isEqualTo(201);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("name").asText()).isEqualTo("Dragan");
            assertThat(body.get("rank").asText()).isEqualTo("NOVICE");
            assertThat(body.get("experiencePoints").asInt()).isZero();
            assertThat(body.get("id").asText()).isNotBlank();
        });
    }

    @Test
    void recruits_a_member_whose_first_name_is_not_in_the_usual_list() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members", Map.of("name", "Bertrand"));

            // Assert
            assertThat(response.code()).isEqualTo(201);
            assertThat(MAPPER.readTree(response.body().string()).get("name").asText()).isEqualTo("Bertrand");
        });
    }

    @Test
    void rejects_a_recruit_whose_name_is_blank() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members", Map.of("name", "   "));

            // Assert
            assertThat(response.code()).isEqualTo(400);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("VALIDATION");
        });
    }

    @Test
    void rejects_a_duplicate_recruit_with_conflict() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 1)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members", Map.of("name", "Dragan"));

            // Assert
            assertThat(response.code()).isEqualTo(409);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("CONFLICT");
        });
    }

    @Test
    void rejects_a_recruit_without_a_name() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(400);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("VALIDATION");
        });
    }

    @Test
    void completes_a_quest_and_credits_the_rewards() {
        // Arrange
        seed(services -> {
            services.members().save(Member.novice("m-dragan", "Dragan", 3));
            assign(services, "m-dragan", "1", QuestAssignmentStatus.ASSIGNED);
        });

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests/1/completion", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("experienceGained").asInt()).isEqualTo(50);
            assertThat(body.get("lootValue").asInt()).isEqualTo(23);
            assertThat(body.get("member").get("experiencePoints").asInt()).isEqualTo(50);
        });
    }

    @Test
    void rejects_completing_a_quest_already_completed() {
        // Arrange
        seed(services -> {
            services.members().save(Member.novice("m-dragan", "Dragan", 3));
            assign(services, "m-dragan", "1", QuestAssignmentStatus.COMPLETED);
        });

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests/1/completion", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(409);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("CONFLICT");
        });
    }

    @Test
    void returns_404_when_completing_an_unknown_quest() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/quests/999/completion", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void returns_404_when_completing_a_quest_for_an_unknown_member() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Ghost/quests/1/completion", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void does_not_promote_a_member_who_is_not_eligible() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/promotion", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("promoted").asBoolean()).isFalse();
            assertThat(body.get("previousRank").asText()).isEqualTo("NOVICE");
            assertThat(body.get("member").get("rank").asText()).isEqualTo("NOVICE");
        });
    }

    @Test
    void promotes_a_member_who_reached_the_threshold() {
        // Arrange
        seed(services -> services.members().save(
                new Member("m-dragan", "Dragan", MemberRank.NOVICE, 100, 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/members/Dragan/promotion", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("promoted").asBoolean()).isTrue();
            assertThat(body.get("previousRank").asText()).isEqualTo("NOVICE");
            assertThat(body.get("member").get("rank").asText()).isEqualTo("APPRENTICE");
        });
    }
}
