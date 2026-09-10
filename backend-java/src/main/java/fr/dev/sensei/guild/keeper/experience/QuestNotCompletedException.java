package fr.dev.sensei.guild.keeper.experience;

import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;

/**
 * Exception metier levee lorsqu'on demande le gain d'experience d'une quete qui
 * n'est pas au statut COMPLETED.
 */
public class QuestNotCompletedException extends RuntimeException {

    public QuestNotCompletedException(String questId, QuestAssignmentStatus actualStatus) {
        super("La quete " + questId + " n'est pas completee (statut : " + actualStatus + ").");
    }
}
