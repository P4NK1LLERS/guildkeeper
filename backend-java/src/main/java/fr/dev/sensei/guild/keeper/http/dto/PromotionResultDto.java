package fr.dev.sensei.guild.keeper.http.dto;

import fr.dev.sensei.guild.keeper.recruitment.Member;

/**
 * Resultat de {@code POST /api/v1/members/{name}/promotion}.
 *
 * <p>{@code promoted} vaut {@code false} quand le membre ne remplit pas les
 * conditions : ce n'est pas une erreur, la reponse reste 200.
 *
 * @param member etat du membre apres l'operation (rang mis a jour si promu)
 */
public record PromotionResultDto(boolean promoted, String previousRank, MemberDto member) {

    public static PromotionResultDto of(boolean promoted, String previousRank, Member updatedMember) {
        return new PromotionResultDto(promoted, previousRank, MemberDto.from(updatedMember));
    }
}
