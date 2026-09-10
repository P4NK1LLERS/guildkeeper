package fr.dev.sensei.guild.keeper.missions;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;

import java.util.List;

/**
 * Orchestration de l'attribution des quetes.
 *
 * <p>Une quete est attribuee a un membre uniquement si :
 * <ul>
 *   <li>le membre n'a pas deja une quete au statut ASSIGNED ;</li>
 *   <li>la quete n'a pas de prerequis, ou ce prerequis a deja ete complete par ce membre.</li>
 * </ul>
 *
 * <p>Le service est sans etat : il lit les attributions existantes via le
 * {@link QuestAssignmentRepository} a chaque appel.
 */
public class QuestAssignmentService {

    private final MemberRepository memberRepository;
    private final QuestRepository questRepository;
    private final QuestAssignmentRepository assignmentRepository;

    public QuestAssignmentService(MemberRepository memberRepository,
                                  QuestRepository questRepository,
                                  QuestAssignmentRepository assignmentRepository) {
        this.memberRepository = memberRepository;
        this.questRepository = questRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Attribue la quete {@code questId} au membre {@code memberId}.
     *
     * @throws IllegalArgumentException si le membre ou la quete est introuvable
     * @throws IllegalStateException    si le membre a deja une quete en cours,
     *                                  ou si un prerequis n'est pas complete
     */
    public QuestAssignment assign(String memberId, String questId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable : " + memberId));
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quete introuvable : " + questId));

        List<QuestAssignment> memberAssignments = assignmentRepository.findByMember(memberId);

        if (hasQuestInProgress(memberAssignments)) {
            throw new IllegalStateException(
                    "Le membre " + member.name() + " a deja une quete en cours.");
        }
        if (quest.hasPrerequisite() && !hasCompleted(memberAssignments, quest.prerequisiteQuestId())) {
            throw new IllegalStateException(
                    "Le prerequis " + quest.prerequisiteQuestId() + " n'est pas complete par " + member.name() + ".");
        }

        return assignmentRepository.add(new QuestAssignment(member, quest));
    }

    private static boolean hasQuestInProgress(List<QuestAssignment> assignments) {
        return assignments.stream()
                .anyMatch(assignment -> assignment.status() == QuestAssignmentStatus.ASSIGNED);
    }

    private static boolean hasCompleted(List<QuestAssignment> assignments, String questId) {
        return assignments.stream()
                .anyMatch(assignment -> assignment.quest().id().equals(questId)
                        && assignment.status() == QuestAssignmentStatus.COMPLETED);
    }
}
