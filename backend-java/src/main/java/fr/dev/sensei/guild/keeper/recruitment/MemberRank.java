package fr.dev.sensei.guild.keeper.recruitment;

/**
 * Rangs d'un membre de la guilde, du plus faible au plus eleve.
 * L'ordre de declaration porte du sens metier : il sert au calcul du bonus
 * d'experience (module experience) et a la promotion (module promotion).
 */
public enum MemberRank {
    NOVICE,
    APPRENTICE,
    VETERAN,
    ELITE,
    GUILD_MASTER
}
