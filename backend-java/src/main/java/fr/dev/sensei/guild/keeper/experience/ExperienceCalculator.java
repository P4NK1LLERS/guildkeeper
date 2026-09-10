package fr.dev.sensei.guild.keeper.experience;

import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.missions.QuestDifficulty;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;

/**
 * Calcul pur de l'experience gagnee par un membre pour une quete completee.
 *
 * <p>Regles :
 * <ul>
 *   <li>base = {@code quest.baseExperienceReward()} ;</li>
 *   <li>bonus de rang : +10% par rang au-dessus de NOVICE (un VETERAN obtient +20%) ;</li>
 *   <li>"coup de pouce" : une quete LEGENDARY completee par un membre NOVICE
 *       accorde un bonus fixe supplementaire de 50 points ;</li>
 *   <li>si la quete n'est pas au statut COMPLETED, une exception metier est levee ;</li>
 *   <li>le gain final est toujours strictement positif.</li>
 * </ul>
 */
public class ExperienceCalculator {

    private static final int LEGENDARY_NOVICE_BOOST = 50;
    private static final double BONUS_PER_RANK = 0.10;

    public int calculateExperienceReward(QuestAssignment assignment) {
        QuestAssignmentStatus status = assignment.status();
        Quest quest = assignment.quest();
        Member member = assignment.member();

        if (status != QuestAssignmentStatus.COMPLETED) {
            throw new QuestNotCompletedException(quest.id(), status);
        }

        int rankStepsAboveNovice = member.rank().ordinal() - MemberRank.NOVICE.ordinal();
        double rankFactor = 1.0 + BONUS_PER_RANK * rankStepsAboveNovice;
        int reward = (int) Math.round(quest.baseExperienceReward() * rankFactor);

        if (quest.difficulty() == QuestDifficulty.LEGENDARY && member.rank() == MemberRank.NOVICE) {
            reward += LEGENDARY_NOVICE_BOOST;
        }

        if (reward <= 0) {
            throw new IllegalStateException(
                    "Le gain d'experience calcule pour la quete " + quest.id() + " est nul ou negatif : " + reward);
        }
        return reward;
    }
}
