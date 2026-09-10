package fr.dev.sensei.guild.keeper.http.dto;

import fr.dev.sensei.guild.keeper.rewards.RewardsDistributionService.RewardsResult;
import fr.dev.sensei.guild.keeper.recruitment.Member;

/**
 * Resultat de {@code POST /api/v1/members/{name}/quests/{id}/completion} :
 * recompenses accordees et etat du membre apres credit de l'experience.
 */
public record RewardsResultDto(int experienceGained, int lootValue, MemberDto member) {

    public static RewardsResultDto of(RewardsResult result, Member updatedMember) {
        return new RewardsResultDto(result.experienceGained(), result.lootValue(), MemberDto.from(updatedMember));
    }
}
