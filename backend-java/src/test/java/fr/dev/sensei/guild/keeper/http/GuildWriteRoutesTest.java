package fr.dev.sensei.guild.keeper.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GuildWriteRoutesTest extends HttpApiTest {

    @Test
    void credits_the_guild_account_on_deposit() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/guild/deposits", Map.of("amount", 500));

            // Assert
            assertThat(response.code()).isEqualTo(200);
            assertThat(MAPPER.readTree(response.body().string()).get("balance").asInt()).isEqualTo(500);
        });
    }

    @Test
    void rejects_a_non_positive_deposit() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/guild/deposits", Map.of("amount", 0));

            // Assert
            assertThat(response.code()).isEqualTo(400);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("VALIDATION");
        });
    }

    @Test
    void rejects_a_deposit_without_an_amount() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/guild/deposits", Map.of());

            // Assert
            assertThat(response.code()).isEqualTo(400);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("VALIDATION");
        });
    }

    @Test
    void debits_the_guild_account_on_loot_distribution() {
        JavalinTest.test(app(), (server, client) -> {
            // Arrange
            client.post("/api/v1/guild/deposits", Map.of("amount", 500));

            // Act
            var response = client.post("/api/v1/guild/loot-distributions", Map.of("amount", 120));

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("balance").asInt()).isEqualTo(380);
        });
    }

    @Test
    void rejects_a_loot_distribution_that_exceeds_the_balance() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.post("/api/v1/guild/loot-distributions", Map.of("amount", 50));

            // Assert
            assertThat(response.code()).isEqualTo(409);
            assertThat(MAPPER.readTree(response.body().string()).get("error").asText()).isEqualTo("CONFLICT");
        });
    }
}
