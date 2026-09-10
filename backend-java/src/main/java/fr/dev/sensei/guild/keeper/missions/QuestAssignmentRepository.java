package fr.dev.sensei.guild.keeper.missions;

import java.util.List;

/**
 * Port de persistance des attributions de quetes.
 *
 * <p>C'est par lui que {@link QuestAssignmentService} relit les attributions
 * existantes a chaque appel : le service reste sans etat.
 */
public interface QuestAssignmentRepository {

    /** Attributions d'un membre, de la plus ancienne a la plus recente. */
    List<QuestAssignment> findByMember(String memberId);

    /** Persiste une nouvelle attribution (statut porte par l'objet). */
    QuestAssignment add(QuestAssignment assignment);

    /**
     * Fait passer a {@code COMPLETED} l'attribution {@code ASSIGNED} du couple
     * (membre, quete). Ne fait rien si aucune attribution en cours n'existe :
     * l'appelant a la charge de verifier au prealable (voir les routes).
     */
    void markCompleted(String memberId, String questId);
}
