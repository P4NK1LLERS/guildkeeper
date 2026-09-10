package fr.dev.sensei.guild.keeper.http;

import com.fasterxml.jackson.databind.JsonNode;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRoutesTest extends HttpApiTest {

    @Test
    void lists_the_members_sorted_by_name() {
        // Arrange
        seed(services -> {
            services.members().save(Member.novice("m-dragan", "Dragan", 3));
            services.members().save(Member.novice("m-alberic", "Albéric", 2));
        });

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/members");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body).hasSize(2);
            assertThat(body.get(0).get("name").asText()).isEqualTo("Albéric");
            assertThat(body.get(1).get("name").asText()).isEqualTo("Dragan");
        });
    }

    @Test
    void lists_no_member_on_a_fresh_guild() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/members");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            assertThat(MAPPER.readTree(response.body().string())).isEmpty();
        });
    }

    @Test
    void returns_a_member_by_name() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dragan", "Dragan", 3)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/members/Dragan");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("id").asText()).isEqualTo("m-dragan");
            assertThat(body.get("name").asText()).isEqualTo("Dragan");
            assertThat(body.get("rank").asText()).isEqualTo("NOVICE");
            assertThat(body.get("experiencePoints").asInt()).isZero();
            assertThat(body.get("luck").asInt()).isEqualTo(3);
        });
    }

    @Test
    void returns_404_with_error_body_for_unknown_member() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/members/Gandalf");

            // Assert
            assertThat(response.code()).isEqualTo(404);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("error").asText()).isEqualTo("NOT_FOUND");
            assertThat(body.get("message").asText()).contains("Gandalf");
        });
    }

    @Test
    void lists_a_member_assignments_with_resolved_titles() {
        // Arrange
        seed(services -> {
            services.members().save(Member.novice("m-dragan", "Dragan", 3));
            assign(services, "m-dragan", "1", QuestAssignmentStatus.COMPLETED);
            assign(services, "m-dragan", "2", QuestAssignmentStatus.ASSIGNED);
        });

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/members/Dragan/assignments");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body).hasSize(2);
            assertThat(body.get(0).get("questTitle").asText()).isEqualTo("Nettoyer les caves de la guilde");
            assertThat(body.get(0).get("status").asText()).isEqualTo("COMPLETED");
            assertThat(body.get(1).get("questTitle").asText()).isEqualTo("Escorter la caravane marchande");
            assertThat(body.get(1).get("status").asText()).isEqualTo("ASSIGNED");
        });
    }

    @Test
    void returns_empty_list_when_member_has_no_assignment() {
        // Arrange
        seed(services -> services.members().save(Member.novice("m-dorian", "Dorian", 2)));

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/members/Dorian/assignments");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body).isEmpty();
        });
    }
}
