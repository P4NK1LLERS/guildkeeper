package fr.dev.sensei.guild.keeper.http.dto;

/** Corps des operations financieres ({@code POST /api/v1/guild/deposits} et {@code .../loot-distributions}). */
public record AmountRequest(Integer amount) {
}
