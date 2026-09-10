package fr.dev.sensei.guild.keeper.http.dto;

/**
 * Etat de synthese de la guilde en sortie de l'API.
 *
 * @param balance     solde du compte de guilde, en pieces d'or
 * @param memberCount nombre de membres
 */
public record GuildStatusDto(int balance, int memberCount) {
}
