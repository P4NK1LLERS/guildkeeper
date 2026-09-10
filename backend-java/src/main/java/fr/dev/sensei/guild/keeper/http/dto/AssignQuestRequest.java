package fr.dev.sensei.guild.keeper.http.dto;

/** Corps de {@code POST /api/v1/members/{name}/quests}. */
public record AssignQuestRequest(String questId) {
}
