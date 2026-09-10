package fr.dev.sensei.guild.keeper.missions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Chapitre 2 — « Convertir des tests dupliqués » : les trois tests
 * {@code should_calculate_loot_with_..._luck}, identiques a la valeur des donnees pres,
 * sont regroupes dans un unique {@link ParameterizedTest} alimente par
 * {@link #lootScenarios()}.
 *
 * <p>Formule verifiee : {@code baseLootValue + baseLootValue * luck / 20} en arithmetique
 * entiere (la division tronque).
 */
class LootCalculatorTest {

    private final LootCalculator lootCalculator = new LootCalculator();

    @ParameterizedTest(name = "baseLootValue={0}, luck={1} -> {2}")
    @MethodSource("lootScenarios")
    void should_calculate_loot_for_various_luck_and_base_values(int baseLootValue, int luck, int expectedLoot) {
        // Act
        int loot = lootCalculator.calculateLoot(baseLootValue, luck);

        // Assert
        assertThat(loot).isEqualTo(expectedLoot);
    }

    static Stream<Arguments> lootScenarios() {
        return Stream.of(
                // les trois cas historiques : chance faible / moyenne / forte
                Arguments.of(100, 2, 110),
                Arguments.of(100, 5, 125),
                Arguments.of(100, 10, 150),
                // bornes de la chance
                Arguments.of(100, 1, 105),
                Arguments.of(200, 10, 300),
                // butin de base nul -> aucun bonus possible
                Arguments.of(0, 10, 0),
                // troncature de la division entiere : 1 * 10 / 20 = 0
                Arguments.of(1, 10, 1),
                Arguments.of(50, 1, 52),
                Arguments.of(80, 3, 92));
    }

    @ParameterizedTest(name = "luck={0} hors bornes -> IllegalArgumentException")
    @ValueSource(ints = {0, -1, 11})
    void should_reject_luck_outside_the_1_to_10_range(int invalidLuck) {
        // Act & Assert
        assertThatThrownBy(() -> lootCalculator.calculateLoot(100, invalidLuck))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(invalidLuck));
    }

    @ParameterizedTest(name = "baseLootValue={0} negatif -> IllegalArgumentException")
    @ValueSource(ints = {-1, -100})
    void should_reject_a_negative_base_loot_value(int negativeBaseLoot) {
        // Act & Assert
        assertThatThrownBy(() -> lootCalculator.calculateLoot(negativeBaseLoot, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(negativeBaseLoot));
    }
}
