package fr.dev.sensei.guild.keeper.rewards;

import fr.dev.sensei.guild.keeper.recruitment.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation de test du {@link NotificationPort} : elle memorise les
 * notifications envoyees pour que les tests puissent les inspecter.
 *
 * <p>Fournie deja ecrite pour que les etudiants n'aient pas a la reconstruire.
 */
public class FakeNotificationPort implements NotificationPort {

    /** Une notification capturee. */
    public record SentNotification(Member member, String message) {
    }

    private final List<SentNotification> sentNotifications = new ArrayList<>();

    @Override
    public void notifyMember(Member member, String message) {
        sentNotifications.add(new SentNotification(member, message));
    }

    public List<SentNotification> sentNotifications() {
        return List.copyOf(sentNotifications);
    }

    public int count() {
        return sentNotifications.size();
    }

    public boolean hasNotified(Member member) {
        return sentNotifications.stream().anyMatch(notification -> notification.member().equals(member));
    }

    public SentNotification lastNotification() {
        if (sentNotifications.isEmpty()) {
            throw new IllegalStateException("Aucune notification n'a ete envoyee.");
        }
        return sentNotifications.get(sentNotifications.size() - 1);
    }
}
