package fr.dev.sensei.guild.keeper.persistence.sqlite;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation SQLite de {@link MemberRepository}.
 *
 * <p>{@code members.id} stocke tel quel l'UUID produit par le domaine
 * ({@code Member.id()}) : aucune information n'est perdue entre le domaine Java
 * et SQLite. Le nom reste par ailleurs {@code UNIQUE} dans le schema.
 */
public class SqliteMemberRepository implements MemberRepository {

    private static final String COLUMNS = "id, name, rank, experience_points, luck";

    private final Connection connection;

    public SqliteMemberRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Member> findById(String id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + COLUMNS + " FROM members WHERE id = ?")) {
            statement.setString(1, id);
            return readOne(statement);
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Lecture du membre " + id + " impossible", e);
        }
    }

    @Override
    public Optional<Member> findByName(String name) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + COLUMNS + " FROM members WHERE name = ?")) {
            statement.setString(1, name);
            return readOne(statement);
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Lecture du membre " + name + " impossible", e);
        }
    }

    @Override
    public List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + COLUMNS + " FROM members ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                members.add(mapRow(resultSet));
            }
            return members;
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Lecture des membres impossible", e);
        }
    }

    @Override
    public void save(Member member) {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO members (id, name, rank, experience_points, luck)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    rank = excluded.rank,
                    experience_points = excluded.experience_points,
                    luck = excluded.luck
                """)) {
            statement.setString(1, member.id());
            statement.setString(2, member.name());
            statement.setString(3, member.rank().name());
            statement.setInt(4, member.experiencePoints());
            statement.setInt(5, member.luck());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Enregistrement du membre " + member.name() + " impossible", e);
        }
    }

    private Optional<Member> readOne(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
        }
    }

    private Member mapRow(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getString("id"),
                resultSet.getString("name"),
                MemberRank.valueOf(resultSet.getString("rank")),
                resultSet.getInt("experience_points"),
                resultSet.getInt("luck"));
    }
}
