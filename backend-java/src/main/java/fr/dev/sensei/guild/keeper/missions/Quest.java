package fr.dev.sensei.guild.keeper.missions;

import java.util.Objects;
import java.util.Optional;

/**
 * Quete du catalogue de la guilde.
 *
 * @param id                    identifiant unique
 * @param title                 intitule
 * @param difficulty            difficulte
 * @param baseExperienceReward  experience de base accordee (avant bonus de rang)
 * @param baseLootValue         butin de base en pieces d'or (avant bonus de chance)
 * @param prerequisiteQuestId   quete a completer au prealable, {@code null} si aucune
 */
public record Quest(
        String id,
        String title,
        QuestDifficulty difficulty,
        int baseExperienceReward,
        int baseLootValue,
        String prerequisiteQuestId) {

    public Quest {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(difficulty, "difficulty");
    }

    /** Fabrique une quete sans prerequis. */
    public static Quest standalone(String id, String title, QuestDifficulty difficulty,
                                   int baseExperienceReward, int baseLootValue) {
        return new Quest(id, title, difficulty, baseExperienceReward, baseLootValue, null);
    }

    public Optional<String> prerequisite() {
        return Optional.ofNullable(prerequisiteQuestId);
    }

    public boolean hasPrerequisite() {
        return prerequisiteQuestId != null;
    }
}
