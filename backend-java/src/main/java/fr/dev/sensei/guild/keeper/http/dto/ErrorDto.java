package fr.dev.sensei.guild.keeper.http.dto;

/**
 * Corps JSON standard des reponses d'erreur.
 *
 * @param error   code stable ({@code NOT_FOUND}, {@code VALIDATION}, {@code CONFLICT}, {@code INTERNAL})
 * @param message message lisible, jamais destine a etre parse
 */
public record ErrorDto(String error, String message) {
}
