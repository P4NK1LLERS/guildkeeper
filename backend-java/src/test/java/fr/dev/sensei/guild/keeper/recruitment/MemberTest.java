package fr.dev.sensei.guild.keeper.recruitment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Atelier du chapitre 2 — test d'effet de bord.
 *
 * <p>{@link Member#addExperience(int)} ne retourne rien : elle agit par effet de bord en
 * modifiant {@code experiencePoints}. Le test interroge donc l'état de l'objet après l'appel.
 */
class MemberTest {

    @Test
    void should_increase_experience_points_when_experience_is_added() {
        // Arrange
        Member attila = Member.novice("m-1", "Attila", 5);

        // Act
        attila.addExperience(60);

        // Assert
        assertThat(attila.experiencePoints()).isEqualTo(60);
    }

    @Test
    void should_accumulate_successive_experience_gains() {
        // Arrange
        Member attila = Member.novice("m-1", "Attila", 5);

        // Act
        attila.addExperience(60);
        attila.addExperience(40);

        // Assert
        assertThat(attila.experiencePoints()).isEqualTo(100);
    }

    @ParameterizedTest(name = "addExperience({0}) -> IllegalArgumentException")
    @ValueSource(ints = {0, -1, -60})
    void should_reject_a_non_positive_experience_gain(int nonPositiveGain) {
        // Arrange
        Member attila = new Member("m-1", "Attila", MemberRank.NOVICE, 20, 5);

        // Act & Assert
        assertThatThrownBy(() -> attila.addExperience(nonPositiveGain))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(nonPositiveGain));

        // le membre reste intact : l'effet de bord n'a pas eu lieu
        assertThat(attila.experiencePoints()).isEqualTo(20);
    }
}
