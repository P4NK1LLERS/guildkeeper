package fr.dev.sensei.guild.keeper.persistence.sqlite;

/**
 * Enveloppe non verifiee des erreurs SQL du sous-package {@code persistence.sqlite}.
 *
 * <p>Les interfaces repository du domaine ne declarent pas d'exception verifiee ;
 * les implementations SQLite traduisent donc les {@link java.sql.SQLException}
 * en cette exception. Le message de la cause (par exemple {@code database is locked})
 * est propage tel quel pour rester lisible dans les logs et la reponse d'erreur.
 */
public class SqlitePersistenceException extends RuntimeException {

    public SqlitePersistenceException(String message, Throwable cause) {
        super(message + " : " + cause.getMessage(), cause);
    }
}
