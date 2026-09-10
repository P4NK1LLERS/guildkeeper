package fr.dev.sensei.guild.keeper.missions;

import fr.dev.sensei.guild.keeper.recruitment.Member;

import java.util.Objects;

/**
 * Attribution d'une quete a un membre. Le statut evolue au cours de la vie de
 * l'attribution (ASSIGNED -> COMPLETED ou ABANDONED).
 */
public class QuestAssignment {

    private final Member member;
    private final Quest quest;
    private QuestAssignmentStatus status;

    public QuestAssignment(Member member, Quest quest) {
        this(member, quest, QuestAssignmentStatus.ASSIGNED);
    }

    public QuestAssignment(Member member, Quest quest, QuestAssignmentStatus status) {
        this.member = Objects.requireNonNull(member, "member");
        this.quest = Objects.requireNonNull(quest, "quest");
        this.status = Objects.requireNonNull(status, "status");
    }

    public Member member() {
        return member;
    }

    public Quest quest() {
        return quest;
    }

    public QuestAssignmentStatus status() {
        return status;
    }

    public void complete() {
        this.status = QuestAssignmentStatus.COMPLETED;
    }

    public void abandon() {
        this.status = QuestAssignmentStatus.ABANDONED;
    }
}
