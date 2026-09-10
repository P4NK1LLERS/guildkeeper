package fr.dev.sensei.guild.keeper.http.dto;

import fr.dev.sensei.guild.keeper.recruitment.Member;

/** Representation d'un membre de la guilde en sortie de l'API. */
public record MemberDto(
        String id,
        String name,
        String rank,
        int experiencePoints,
        int luck) {

    public static MemberDto from(Member member) {
        return new MemberDto(
                member.id(),
                member.name(),
                member.rank().name(),
                member.experiencePoints(),
                member.luck());
    }
}
