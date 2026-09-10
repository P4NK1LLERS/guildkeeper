package fr.dev.sensei.guild.keeper.persistence.sqlite;

import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentRepository;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.missions.QuestRepository;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation SQLite de {@link QuestAssignmentRepository} (table
 * {@code quest_assignments}).
 *
 * <p>{@code quest_id} est un entier ; {@code member_id} l'UUID du domaine. Les
 * lignes sont reconstruites en {@link QuestAssignment} en resolvant le membre et
 * la quete via leurs repositories respectifs (memes connexion).
 */
public class SqliteQuestAssignmentRepository implements QuestAssignmentRepository {

    private final Connection connection;
    private final MemberRepository memberRepository;
    private final QuestRepository questRepository;

    public SqliteQuestAssignmentRepository(Connection connection) {
        this(connection, new SqliteMemberRepository(connection), new SqliteQuestRepository(connection));
    }

    public SqliteQuestAssignmentRepository(Connection connection,
                                           MemberRepository memberRepository,
                                           QuestRepository questRepository) {
        this.connection = connection;
        this.memberRepository = memberRepository;
        this.questRepository = questRepository;
    }

    @Override
    public List<QuestAssignment> findByMember(String memberId) {
        List<QuestAssignment> assignments = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT quest_id, status FROM quest_assignments WHERE member_id = ? ORDER BY id")) {
            statement.setString(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String questId = String.valueOf(resultSet.getInt("quest_id"));
                    QuestAssignmentStatus status = QuestAssignmentStatus.valueOf(resultSet.getString("status"));
                    toAssignment(memberId, questId, status).ifPresent(assignments::add);
                }
            }
            return assignments;
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Lecture des attributions du membre " + memberId + " impossible", e);
        }
    }

    @Override
    public QuestAssignment add(QuestAssignment assignment) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO quest_assignments (member_id, quest_id, status) VALUES (?, ?, ?)")) {
            statement.setString(1, assignment.member().id());
            statement.setInt(2, Integer.parseInt(assignment.quest().id()));
            statement.setString(3, assignment.status().name());
            statement.executeUpdate();
            return assignment;
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Enregistrement de l'attribution de quete impossible", e);
        }
    }

    @Override
    public void markCompleted(String memberId, String questId) {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE quest_assignments SET status = ?
                WHERE id = (
                    SELECT id FROM quest_assignments
                    WHERE member_id = ? AND quest_id = ? AND status = ?
                    ORDER BY id DESC LIMIT 1
                )
                """)) {
            statement.setString(1, QuestAssignmentStatus.COMPLETED.name());
            statement.setString(2, memberId);
            statement.setInt(3, Integer.parseInt(questId));
            statement.setString(4, QuestAssignmentStatus.ASSIGNED.name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SqlitePersistenceException("Mise a jour de l'attribution de quete impossible", e);
        }
    }

    private Optional<QuestAssignment> toAssignment(String memberId, String questId, QuestAssignmentStatus status) {
        Optional<Member> member = memberRepository.findById(memberId);
        Optional<Quest> quest = questRepository.findById(questId);
        if (member.isEmpty() || quest.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new QuestAssignment(member.get(), quest.get(), status));
    }
}
