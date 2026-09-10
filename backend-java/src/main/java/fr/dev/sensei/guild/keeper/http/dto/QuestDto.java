package fr.dev.sensei.guild.keeper.http.dto;

import fr.dev.sensei.guild.keeper.missions.Quest;

/**
 * Representation d'une quete du catalogue en sortie de l'API.
 *
 * @param prerequisiteQuestId {@code null} si la quete n'a pas de prerequis
 */
public record QuestDto(
        String id,
        String title,
        String difficulty,
        int baseExperienceReward,
        int baseLootValue,
        String prerequisiteQuestId) {

    public static QuestDto from(Quest quest) {
        return new QuestDto(
                quest.id(),
                quest.title(),
                quest.difficulty().name(),
                quest.baseExperienceReward(),
                quest.baseLootValue(),
                quest.prerequisiteQuestId());
    }
}
