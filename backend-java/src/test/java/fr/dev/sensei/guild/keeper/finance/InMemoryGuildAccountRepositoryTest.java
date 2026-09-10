package fr.dev.sensei.guild.keeper.finance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de l'{@link InMemoryGuildAccountRepository}.
 *
 * <p>C'est le double utilise par les tests de bout en bout a la place de
 * l'adaptateur SQLite : il doit se comporter comme le contrat
 * {@link GuildAccountRepository} l'annonce, sinon les tests qui s'appuient sur
 * lui mentent.
 */
class InMemoryGuildAccountRepositoryTest {

    private final GuildAccountRepository repository = new InMemoryGuildAccountRepository();

    @Test
    void should_return_empty_when_the_guild_is_unknown() {
        // Act & Assert
        assertThat(repository.findByGuildId("g-404")).isEmpty();
    }

    @Test
    void should_find_back_a_saved_account_by_its_guild_id() {
        // Arrange
        GuildAccount account = new GuildAccount("g-1", 500);

        // Act
        repository.save(account);

        // Assert
        assertThat(repository.findByGuildId("g-1")).containsSame(account);
    }

    @Test
    void should_keep_the_accounts_of_distinct_guilds_apart() {
        // Arrange
        GuildAccount first = new GuildAccount("g-1", 500);
        GuildAccount second = new GuildAccount("g-2", 120);

        // Act
        repository.save(first);
        repository.save(second);

        // Assert
        assertThat(repository.findByGuildId("g-1")).containsSame(first);
        assertThat(repository.findByGuildId("g-2")).containsSame(second);
    }

    @Test
    void should_replace_the_previous_account_when_the_same_guild_is_saved_again() {
        // Arrange
        repository.save(new GuildAccount("g-1", 500));
        GuildAccount updated = new GuildAccount("g-1", 620);

        // Act
        repository.save(updated);

        // Assert : un seul compte par guilde, le dernier ecrit gagne
        assertThat(repository.findByGuildId("g-1")).containsSame(updated);
    }
}
