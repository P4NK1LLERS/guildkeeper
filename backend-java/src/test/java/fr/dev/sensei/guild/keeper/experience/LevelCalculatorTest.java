package fr.dev.sensei.guild.keeper.experience;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Chapitre 5 — live coding « TDD sur le calcul de niveau ».
 *
 * <p>{@link LevelCalculator} a ete fait naitre de ces tests, en suivant le cycle
 * red / green / refactor :
 * <ol>
 *   <li>red : {@code should_return_level_1_for_zero_experience} -> creation de la classe
 *       et retour constant {@code 1} ;</li>
 *   <li>refactor : {@code should_return_level_2_from_100_experience_points} force a
 *       generaliser en {@code 1 + xp / 100} ;</li>
 *   <li>cas limite : une experience negative est refusee.</li>
 * </ol>
 */
class LevelCalculatorTest {

    private final LevelCalculator calculator = new LevelCalculator();

    @Test
    void should_return_level_1_for_zero_experience() {
        // Act
        int level = calculator.levelFor(0);

        // Assert
        assertThat(level).isEqualTo(1);
    }

    @Test
    void should_return_level_2_from_100_experience_points() {
        // Act
        int level = calculator.levelFor(100);

        // Assert
        assertThat(level).isEqualTo(2);
    }

    @ParameterizedTest(name = "{0} XP -> niveau {1}")
    @CsvSource({"0, 1", "99, 1", "100, 2", "199, 2", "250, 3", "1500, 16"})
    void should_gain_one_level_every_100_experience_points(int experiencePoints, int expectedLevel) {
        // Act
        int level = calculator.levelFor(experiencePoints);

        // Assert
        assertThat(level).isEqualTo(expectedLevel);
    }

    @ParameterizedTest(name = "{0} XP -> IllegalArgumentException")
    @ValueSource(ints = {-1, -100})
    void should_reject_negative_experience(int negativeExperience) {
        // Act & Assert
        assertThatThrownBy(() -> calculator.levelFor(negativeExperience))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(negativeExperience));
    }
}
