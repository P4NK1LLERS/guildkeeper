package fr.dev.sensei.guild.keeper.rewards;

import fr.dev.sensei.guild.keeper.experience.ExperienceCalculator;
import fr.dev.sensei.guild.keeper.missions.LootCalculator;
import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;

/**
 * Calcule, sans effet de bord, la recompense qu'une quete accorderait.
 *
 * <p>Sert de valeur autoritative a l'endpoint {@code reward-preview} : le module
 * TypeScript reproduit ces formules cote client et un test de contrat verifie
 * qu'il reste d'accord avec le serveur (voir {@code docs/architecture.md} §5.3).
 *
 * <p>L'estimation se place du point de vue d'un membre {@code NOVICE} : c'est le
 * cas de reference (aucun bonus de rang), la chance restant parametrable.
 */
public class RewardEstimator {

    private final ExperienceCalculator experienceCalculator;
    private final LootCalculator lootCalculator;

    public RewardEstimator(ExperienceCalculator experienceCalculator, LootCalculator lootCalculator) {
        this.experienceCalculator = experienceCalculator;
        this.lootCalculator = lootCalculator;
    }

    public RewardEstimator() {
        this(new ExperienceCalculator(), new LootCalculator());
    }

    /**
     * @param luck chance du membre, entre 1 et 10
     * @throws IllegalArgumentException si {@code luck} est hors bornes
     */
    public Estimate estimateForNovice(Quest quest, int luck) {
        Member reference = new Member("preview", "preview", MemberRank.NOVICE, 0, luck);
        QuestAssignment completed = new QuestAssignment(reference, quest, QuestAssignmentStatus.COMPLETED);

        int experience = experienceCalculator.calculateExperienceReward(completed);
        int loot = lootCalculator.calculateLoot(quest.baseLootValue(), luck);
        return new Estimate(experience, loot);
    }

    /** Recompense estimee : experience et butin en pieces d'or. */
    public record Estimate(int experience, int loot) {
    }
}
