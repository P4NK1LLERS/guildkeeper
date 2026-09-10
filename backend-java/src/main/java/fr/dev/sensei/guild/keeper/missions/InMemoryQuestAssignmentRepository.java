package fr.dev.sensei.guild.keeper.missions;

import java.util.ArrayList;
import java.util.List;

/** Implementation in-memory du {@link QuestAssignmentRepository}. */
public class InMemoryQuestAssignmentRepository implements QuestAssignmentRepository {

    private final List<QuestAssignment> assignments = new ArrayList<>();

    @Override
    public List<QuestAssignment> findByMember(String memberId) {
        return assignments.stream()
                .filter(assignment -> assignment.member().id().equals(memberId))
                .toList();
    }

    @Override
    public QuestAssignment add(QuestAssignment assignment) {
        assignments.add(assignment);
        return assignment;
    }

    @Override
    public void markCompleted(String memberId, String questId) {
        for (int i = assignments.size() - 1; i >= 0; i--) {
            QuestAssignment assignment = assignments.get(i);
            if (assignment.member().id().equals(memberId)
                    && assignment.quest().id().equals(questId)
                    && assignment.status() == QuestAssignmentStatus.ASSIGNED) {
                assignment.complete();
                return;
            }
        }
    }
}
