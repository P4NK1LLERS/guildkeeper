package fr.dev.sensei.guild.keeper.persistence.sqlite;

import fr.dev.sensei.guild.keeper.finance.GuildAccount;
import fr.dev.sensei.guild.keeper.finance.GuildAccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementation SQLite de {@link GuildAccountRepository}.
 *
 * <p>Le schema ne modelise qu'un seul compte de guilde (une unique ligne dans
 * {@code guild_account}, creee a l'initialisation). Le {@code guildId} fourni est
 * conserve tel quel sur l'objet retourne mais n'est pas persiste : toutes les
 * lectures et ecritures visent la ligne unique.
 */
public class SqliteGuildAccountRepository implements GuildAccountRepository {

    private final Connection connection;

    public SqliteGuildAccountRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<GuildAccount> findByGuildId(String guildId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT balance FROM guild_account ORDER BY id LIMIT 1");
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next()
                    ? Optional.of(new GuildAccount(guildId, resultSet.getInt("balance")))
                    : Optional.empty();
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Lecture du compte de guilde impossible", e);
        }
    }

    @Override
    public void save(GuildAccount account) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE guild_account SET balance = ? WHERE id = (SELECT id FROM guild_account ORDER BY id LIMIT 1)")) {
            statement.setInt(1, account.balance());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Enregistrement du compte de guilde impossible", e);
        }
    }
}
