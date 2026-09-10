package fr.dev.sensei.guild.keeper.http.dto;

import fr.dev.sensei.guild.keeper.rewards.RewardEstimator.Estimate;

/**
 * Reponse de {@code GET /api/v1/quests/{id}/reward-preview?luck={n}} :
 * recompense qu'un membre {@code NOVICE} avec la chance donnee obtiendrait.
 *
 * <p>Valeur autoritative : le module TypeScript reproduit ce calcul et un test
 * de contrat verifie qu'ils restent d'accord.
 */
public record RewardPreviewDto(String questId, int luck, int experience, int loot) {

    public static RewardPreviewDto of(String questId, int luck, Estimate estimate) {
        return new RewardPreviewDto(questId, luck, estimate.experience(), estimate.loot());
    }
}
