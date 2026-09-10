package fr.dev.sensei.guild.keeper.persistence.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ouvre la connexion vers le fichier SQLite du serveur {@code guildkeeper.db}, active
 * le mode WAL et le {@code busy_timeout}, et cree le schema (tables + ligne unique
 * du compte de guilde + catalogue de quetes) s'il est absent.
 *
 * <p>Chemin du fichier : variable d'environnement {@code GUILDKEEPER_DB} si definie,
 * sinon {@code guildkeeper.db} relatif au repertoire de travail (lancer le serveur
 * depuis {@code backend-java/}).
 */
public final class SqliteConnectionFactory {

    /** Nom de la variable d'environnement permettant de surcharger le chemin du fichier. */
    public static final String DB_PATH_ENV = "GUILDKEEPER_DB";

    private static final String DEFAULT_DB_FILE = "guildkeeper.db";

    private final String jdbcUrl;

    public SqliteConnectionFactory() {
        this(resolveDefaultPath());
    }

    public SqliteConnectionFactory(Path databasePath) {
        this.jdbcUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
    }

    private static Path resolveDefaultPath() {
        String override = System.getenv(DB_PATH_ENV);
        return Path.of(override != null && !override.isBlank() ? override : DEFAULT_DB_FILE);
    }

    /**
     * Ouvre une connexion configuree (WAL + {@code busy_timeout} 5000 ms + cles
     * etrangeres) et garantit la presence du schema (tables + ligne du compte +
     * catalogue de quetes). A appeler une fois au demarrage.
     */
    public Connection open() {
        Connection connection = openRaw();
        try {
            initializeSchema(connection);
            return connection;
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Initialisation du schema SQLite impossible", e);
        }
    }

    /**
     * Ouvre une connexion configuree <em>sans</em> (re)creer le schema : a
     * reserver aux appels ou le schema est deja garanti (le serveur l'initialise
     * une fois au demarrage, puis ouvre une connexion par requete).
     */
    public Connection openRaw() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode = WAL");
                statement.execute("PRAGMA busy_timeout = 5000");
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Impossible d'ouvrir la base SQLite (" + jdbcUrl + ")", e);
        }
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // members.id : UUID produit par le domaine (Member.id()), stocke tel quel.
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS members (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL UNIQUE,
                        rank TEXT NOT NULL,
                        experience_points INTEGER NOT NULL DEFAULT 0,
                        luck INTEGER NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS quests (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        base_experience_reward INTEGER NOT NULL,
                        base_loot_value INTEGER NOT NULL,
                        prerequisite_quest_id INTEGER REFERENCES quests(id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS quest_assignments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        member_id TEXT NOT NULL REFERENCES members(id),
                        quest_id INTEGER NOT NULL REFERENCES quests(id),
                        status TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS guild_account (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        balance INTEGER NOT NULL DEFAULT 0
                    )
                    """);
            // Compte unique de la guilde : une seule ligne, creee si absente.
            statement.execute("INSERT INTO guild_account (balance) SELECT 0 "
                    + "WHERE NOT EXISTS (SELECT 1 FROM guild_account)");
        }
        seedQuestCatalog(connection);
    }

    /**
     * Insere le catalogue de quetes de reference si la table est vide. Chaine de
     * prerequis lineaire (caves -> caravane -> dragon) qui sert de donnees de
     * reference a l'API.
     */
    private static void seedQuestCatalog(Connection connection) throws SQLException {
        try (Statement check = connection.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM quests")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO quests (title, difficulty, base_experience_reward, base_loot_value, prerequisite_quest_id)
                    VALUES ('Nettoyer les caves de la guilde', 'EASY', 50, 20, NULL)
                    """);
            statement.executeUpdate("""
                    INSERT INTO quests (title, difficulty, base_experience_reward, base_loot_value, prerequisite_quest_id)
                    VALUES ('Escorter la caravane marchande', 'MEDIUM', 120, 60,
                            (SELECT id FROM quests WHERE title = 'Nettoyer les caves de la guilde'))
                    """);
            statement.executeUpdate("""
                    INSERT INTO quests (title, difficulty, base_experience_reward, base_loot_value, prerequisite_quest_id)
                    VALUES ('Terrasser le dragon des cimes', 'LEGENDARY', 500, 300,
                            (SELECT id FROM quests WHERE title = 'Escorter la caravane marchande'))
                    """);
        }
    }
}
