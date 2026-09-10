package fr.dev.sensei.guild.keeper.http.dto;

/**
 * Representation d'une attribution de quete en sortie de l'API.
 *
 * <p>Le titre de la quete est resolu cote serveur : le client n'a pas a
 * connaitre les identifiants techniques.
 *
 * @param status {@code ASSIGNED}, {@code COMPLETED} ou {@code ABANDONED}
 */
public record AssignmentDto(
        String questId,
        String questTitle,
        String status) {
}
