package fr.dev.sensei.guild.keeper.persistence.sqlite;

import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestDifficulty;
import fr.dev.sensei.guild.keeper.missions.QuestRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation SQLite de {@link QuestRepository}.
 *
 * <p>{@code Quest.id()} et {@code Quest.prerequisiteQuestId()} exposent les valeurs
 * entieres des colonnes {@code id} / {@code prerequisite_quest_id} sous forme de
 * chaine.
 */
public class SqliteQuestRepository implements QuestRepository {

    private static final String COLUMNS =
            "id, title, difficulty, base_experience_reward, base_loot_value, prerequisite_quest_id";

    private final Connection connection;

    public SqliteQuestRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Quest> findById(String id) {
        Integer rowId = tryParseInt(id);
        if (rowId == null) {
            return Optional.empty();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + COLUMNS + " FROM quests WHERE id = ?")) {
            statement.setInt(1, rowId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Lecture de la quete " + id + " impossible", e);
        }
    }

    @Override
    public List<Quest> findAll() {
        List<Quest> quests = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + COLUMNS + " FROM quests ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                quests.add(mapRow(resultSet));
            }
            return quests;
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Lecture du catalogue de quetes impossible", e);
        }
    }

    @Override
    public void save(Quest quest) {
        Integer existingId = tryParseInt(quest.id());
        try {
            if (existingId != null && exists(existingId)) {
                update(existingId, quest);
            } else {
                insert(quest);
            }
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Enregistrement de la quete \"" + quest.title() + "\" impossible", e);
        }
    }

    private boolean exists(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM quests WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insert(Quest quest) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO quests (title, difficulty, base_experience_reward, base_loot_value, prerequisite_quest_id)
                VALUES (?, ?, ?, ?, ?)
                """)) {
            bindBody(statement, quest);
            statement.executeUpdate();
        }
    }

    private void update(int id, Quest quest) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE quests SET title = ?, difficulty = ?, base_experience_reward = ?,
                    base_loot_value = ?, prerequisite_quest_id = ?
                WHERE id = ?
                """)) {
            bindBody(statement, quest);
            statement.setInt(6, id);
            statement.executeUpdate();
        }
    }

    private void bindBody(PreparedStatement statement, Quest quest) throws SQLException {
        statement.setString(1, quest.title());
        statement.setString(2, quest.difficulty().name());
        statement.setInt(3, quest.baseExperienceReward());
        statement.setInt(4, quest.baseLootValue());
        Integer prerequisite = tryParseInt(quest.prerequisiteQuestId());
        if (prerequisite == null) {
            statement.setNull(5, java.sql.Types.INTEGER);
        } else {
            statement.setInt(5, prerequisite);
        }
    }

    private Quest mapRow(ResultSet resultSet) throws SQLException {
        int prerequisiteId = resultSet.getInt("prerequisite_quest_id");
        String prerequisite = resultSet.wasNull() ? null : String.valueOf(prerequisiteId);
        return new Quest(
                String.valueOf(resultSet.getInt("id")),
                resultSet.getString("title"),
                QuestDifficulty.valueOf(resultSet.getString("difficulty")),
                resultSet.getInt("base_experience_reward"),
                resultSet.getInt("base_loot_value"),
                prerequisite);
    }

    private static Integer tryParseInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
