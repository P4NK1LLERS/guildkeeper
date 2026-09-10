package fr.dev.sensei.guild.keeper.http;

import com.fasterxml.jackson.databind.JsonNode;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuildRoutesTest extends HttpApiTest {

    @Test
    void reports_zero_balance_and_no_member_on_a_fresh_guild() {
        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/guild");

            // Assert
            assertThat(response.code()).isEqualTo(200);
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("balance").asInt()).isZero();
            assertThat(body.get("memberCount").asInt()).isZero();
        });
    }

    @Test
    void counts_the_members_of_the_guild() {
        // Arrange
        seed(services -> {
            services.members().save(Member.novice("m-1", "Dragan", 3));
            services.members().save(Member.novice("m-2", "Dorian", 2));
        });

        JavalinTest.test(app(), (server, client) -> {
            // Act
            var response = client.get("/api/v1/guild");

            // Assert
            JsonNode body = MAPPER.readTree(response.body().string());
            assertThat(body.get("memberCount").asInt()).isEqualTo(2);
        });
    }
}
