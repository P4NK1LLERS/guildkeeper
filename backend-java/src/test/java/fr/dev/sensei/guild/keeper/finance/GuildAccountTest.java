package fr.dev.sensei.guild.keeper.finance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests d'etat du {@link GuildAccount}.
 *
 * <p>L'entite n'a aucune dependance : rien a isoler ici, on verifie ses
 * invariants directement. Les mutations {@code increaseBy}/{@code decreaseBy}
 * sont package-private (reservees au {@link GuildFinanceService}) : ce test est
 * dans le meme package et peut donc les appeler.
 */
class GuildAccountTest {

    @Test
    void should_expose_its_guild_id_and_its_initial_balance() {
        // Arrange & Act
        GuildAccount account = new GuildAccount("g-1", 250);

        // Assert
        assertThat(account.guildId()).isEqualTo("g-1");
        assertThat(account.balance()).isEqualTo(250);
    }

    @Test
    void should_accept_an_empty_account() {
        // Arrange & Act & Assert : 0 est la borne basse valide
        assertThat(new GuildAccount("g-1", 0).balance()).isZero();
    }

    @ParameterizedTest(name = "solde initial {0} -> refuse")
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    void should_reject_a_negative_initial_balance(int negativeBalance) {
        // Act & Assert
        assertThatThrownBy(() -> new GuildAccount("g-1", negativeBalance))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(negativeBalance));
    }

    @Test
    void should_reject_a_null_guild_id() {
        // Act & Assert
        assertThatThrownBy(() -> new GuildAccount(null, 100))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("guildId");
    }

    @Test
    void should_increase_the_balance() {
        // Arrange
        GuildAccount account = new GuildAccount("g-1", 100);

        // Act
        account.increaseBy(50);

        // Assert
        assertThat(account.balance()).isEqualTo(150);
    }

    @Test
    void should_decrease_the_balance() {
        // Arrange
        GuildAccount account = new GuildAccount("g-1", 100);

        // Act
        account.decreaseBy(30);

        // Assert
        assertThat(account.balance()).isEqualTo(70);
    }

    @Test
    void should_allow_a_debit_equal_to_the_whole_balance() {
        // Arrange
        GuildAccount account = new GuildAccount("g-1", 100);

        // Act
        account.decreaseBy(100);

        // Assert
        assertThat(account.balance()).isZero();
    }

    @Test
    void should_reject_a_debit_above_the_balance_and_keep_the_balance_untouched() {
        // Arrange
        GuildAccount account = new GuildAccount("g-1", 100);

        // Act & Assert : dernier rempart, le solde ne devient jamais negatif
        assertThatThrownBy(() -> account.decreaseBy(101))
                .isInstanceOf(IllegalStateException.class);
        assertThat(account.balance()).isEqualTo(100);
    }
}
