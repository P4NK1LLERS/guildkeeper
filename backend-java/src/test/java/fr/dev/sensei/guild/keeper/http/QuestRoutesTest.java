package fr.dev.sensei.guild.keeper.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestRoutesTest extends HttpApiTest {

    @Test
    void lists_the_seeded_quest_catalog() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body).hasSize(3);
            assertThat(body.get(0).get("title").asText()).isEqualTo("Nettoyer les caves de la guilde");
            assertThat(body.get(0).get("difficulty").asText()).isEqualTo("EASY");
            assertThat(body.get(0).get("prerequisiteQuestId").isNull()).isTrue();
            assertThat(body.get(1).get("prerequisiteQuestId").asText()).isEqualTo("1");
        });
    }

    @Test
    void returns_one_quest_by_id() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests/3");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("title").asText()).isEqualTo("Terrasser le dragon des cimes");
            assertThat(body.get("baseExperienceReward").asInt()).isEqualTo(500);
        });
    }

    @Test
    void returns_404_with_error_body_for_unknown_quest() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests/999");

            // Assert
            assertThat(response.code()).isEqualTo(404);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("error").asText()).isEqualTo("NOT_FOUND");
            assertThat(body.get("message").asText()).contains("999");
        });
    }

    @Test
    void previews_the_reward_of_an_easy_quest() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests/1/reward-preview?luck=1");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("questId").asText()).isEqualTo("1");
            assertThat(body.get("luck").asInt()).isEqualTo(1);
            assertThat(body.get("experience").asInt()).isEqualTo(50);
            assertThat(body.get("loot").asInt()).isEqualTo(21);
        });
    }

    @Test
    void previews_the_reward_of_a_legendary_quest_with_the_novice_boost() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests/3/reward-preview?luck=5");

            // Assert
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("experience").asInt()).isEqualTo(550); // 500 + boost NOVICE/LEGENDARY
            assertThat(body.get("loot").asInt()).isEqualTo(375);       // 300 + 300*5/20
        });
    }

    @Test
    void rejects_a_reward_preview_without_luck() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests/1/reward-preview");

            // Assert
            assertThat(response.code()).isEqualTo(400);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("VALIDATION");
        });
    }

    @Test
    void rejects_a_reward_preview_with_out_of_range_luck() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests/1/reward-preview?luck=42");

            // Assert
            assertThat(response.code()).isEqualTo(400);
        });
    }

    @Test
    void returns_404_for_a_reward_preview_of_an_unknown_quest() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/quests/999/reward-preview?luck=3");

            // Assert
            assertThat(response.code()).isEqualTo(404);
        });
    }
}
