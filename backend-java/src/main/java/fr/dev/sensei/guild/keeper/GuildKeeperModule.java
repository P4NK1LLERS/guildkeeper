package fr.dev.sensei.guild.keeper;

import fr.dev.sensei.guild.keeper.persistence.sqlite.SqliteConnectionFactory;
import fr.dev.sensei.guild.keeper.persistence.sqlite.SqlitePersistenceException;
import fr.dev.sensei.guild.keeper.rewards.NotificationPort;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Composition root de GuildKeeper : c'est le seul endroit qui sait comment cabler
 * les repositories et les services metier sur la persistance.
 *
 * <p>Le schema est cree une fois a la construction. Ensuite, chaque unite de
 * travail ({@link #execute} / {@link #run}) ouvre une connexion, s'execute dans
 * une transaction (commit en sortie, rollback si une exception remonte), puis
 * referme la connexion. Pas d'etat partage entre threads.
 */
public final class GuildKeeperModule {

    private static final String GUILD_ID = "guilde-principale";

    private final SqliteConnectionFactory connectionFactory;
    private final NotificationPort notificationPort;

    public GuildKeeperModule(SqliteConnectionFactory connectionFactory) {
        this(connectionFactory, LoggingNotificationPort.INSTANCE);
    }

    public GuildKeeperModule(SqliteConnectionFactory connectionFactory, NotificationPort notificationPort) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory");
        this.notificationPort = Objects.requireNonNull(notificationPort, "notificationPort");
        ensureSchema();
    }

    /** Cable sur le fichier SQLite resolu par l'environnement ({@code GUILDKEEPER_DB} ou defaut). */
    public static GuildKeeperModule fromEnvironment() {
        return new GuildKeeperModule(new SqliteConnectionFactory());
    }

    /** Cable sur un fichier SQLite explicite (tests, environnements dedies). */
    public static GuildKeeperModule usingDatabase(Path databaseFile) {
        return new GuildKeeperModule(new SqliteConnectionFactory(databaseFile));
    }

    /** Ouvre une connexion, execute {@code work} dans une transaction, referme la connexion. */
    public <T> T execute(Function<GuildKeeperServices, T> work) {
        try (Connection connection = connectionFactory.openRaw()) {
            connection.setAutoCommit(false);
            try {
                T result = work.apply(new GuildKeeperServices(connection, GUILD_ID, notificationPort));
                connection.commit();
                return result;
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Operation SQLite impossible", e);
        }
    }

    /** Variante sans valeur de retour de {@link #execute}. */
    public void run(Consumer<GuildKeeperServices> work) {
        execute(services -> {
            work.accept(services);
            return null;
        });
    }

    private void ensureSchema() {
        try (Connection connection = connectionFactory.open()) {
            // open() cree le schema ; on ferme aussitot, execute() ouvre ses propres connexions.
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Fermeture de la connexion d'initialisation impossible", e);
        }
    }
}
