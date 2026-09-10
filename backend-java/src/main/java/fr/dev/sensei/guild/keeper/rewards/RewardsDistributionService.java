package fr.dev.sensei.guild.keeper.rewards;

import fr.dev.sensei.guild.keeper.experience.ExperienceCalculator;
import fr.dev.sensei.guild.keeper.missions.LootCalculator;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;

/**
 * Distribution des recompenses apres completion d'une quete.
 *
 * <p>Deroule :
 * <ol>
 *   <li>calcul de l'experience via {@link ExperienceCalculator} et credit au membre ;</li>
 *   <li>calcul du butin via {@link LootCalculator} ;</li>
 *   <li>persistance du membre ;</li>
 *   <li>notification du membre via {@link NotificationPort}.</li>
 * </ol>
 *
 * <p>Note : le modele metier ne donne pas de "cagnotte" personnelle au membre.
 * Le butin calcule est donc retourne dans le {@link RewardsResult} et mentionne
 * dans la notification, mais n'est pas stocke sur l'entite Member.
 */
public class RewardsDistributionService {

    private final MemberRepository memberRepository;
    private final NotificationPort notificationPort;
    private final ExperienceCalculator experienceCalculator;
    private final LootCalculator lootCalculator;

    public RewardsDistributionService(MemberRepository memberRepository,
                                      NotificationPort notificationPort,
                                      ExperienceCalculator experienceCalculator,
                                      LootCalculator lootCalculator) {
        this.memberRepository = memberRepository;
        this.notificationPort = notificationPort;
        this.experienceCalculator = experienceCalculator;
        this.lootCalculator = lootCalculator;
    }

    public RewardsResult distributeRewards(QuestAssignment assignment) {
        if (assignment.status() != QuestAssignmentStatus.COMPLETED) {
            throw new IllegalStateException("La quete doit etre completee avant de distribuer les recompenses.");
        }

        Member member = assignment.member();
        int experienceGained = experienceCalculator.calculateExperienceReward(assignment);
        int lootValue = lootCalculator.calculateLoot(assignment.quest().baseLootValue(), member.luck());

        member.addExperience(experienceGained);
        memberRepository.save(member);

        notificationPort.notifyMember(member,
                "Quete \"%s\" terminee : +%d XP, +%d pieces d'or."
                        .formatted(assignment.quest().title(), experienceGained, lootValue));

        return new RewardsResult(experienceGained, lootValue);
    }

    /** Recompenses accordees pour une quete completee. */
    public record RewardsResult(int experienceGained, int lootValue) {
    }
}
