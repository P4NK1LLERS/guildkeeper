package fr.dev.sensei.guild.keeper;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.rewards.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation par defaut de {@link NotificationPort} cote serveur : ecrit la
 * notification dans les logs. Une vraie implementation (email, message in-game)
 * n'a pas d'interet pour le cours.
 */
final class LoggingNotificationPort implements NotificationPort {

    static final LoggingNotificationPort INSTANCE = new LoggingNotificationPort();

    private static final Logger LOG = LoggerFactory.getLogger(LoggingNotificationPort.class);

    private LoggingNotificationPort() {
    }

    @Override
    public void notifyMember(Member member, String message) {
        LOG.info("Notification -> {} : {}", member.name(), message);
    }
}
