package fr.dev.sensei.guild.keeper.missions;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestAssignmentServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private QuestRepository questRepository;

    private final QuestAssignmentRepository assignmentRepository = new InMemoryQuestAssignmentRepository();

    @Test
    void should_assign_quest_when_member_is_free_and_prerequisites_are_met() {
        // Arrange
        Member alberic = new Member("m-1", "Albéric", MemberRank.NOVICE, 0, 5);
        Quest quest = Quest.standalone("q-1", "Escorter la caravane", QuestDifficulty.MEDIUM, 120, 60);
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-1")).thenReturn(Optional.of(quest));
        QuestAssignmentService service =
                new QuestAssignmentService(memberRepository, questRepository, assignmentRepository);

        // Act
        QuestAssignment assignment = service.assign("m-1", "q-1");

        // Assert
        assertThat(assignment.member()).isEqualTo(alberic);
        assertThat(assignment.quest()).isEqualTo(quest);
        assertThat(assignment.status()).isEqualTo(QuestAssignmentStatus.ASSIGNED);
        assertThat(assignmentRepository.findByMember("m-1")).containsExactly(assignment);
    }

    // Chapitre 5 — « Attribuer une quête en TDD » (approche par les interactions)
    @Test
    void should_reject_assignment_when_member_already_has_an_assigned_quest() {
        // Arrange
        Member alberic = new Member("m-1", "Albéric", MemberRank.NOVICE, 0, 5);
        Quest ongoing = Quest.standalone("q-1", "Escorter la caravane", QuestDifficulty.MEDIUM, 120, 60);
        Quest wanted = Quest.standalone("q-2", "Purger le donjon", QuestDifficulty.HARD, 200, 100);
        assignmentRepository.add(new QuestAssignment(alberic, ongoing, QuestAssignmentStatus.ASSIGNED));
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-2")).thenReturn(Optional.of(wanted));
        QuestAssignmentService service =
                new QuestAssignmentService(memberRepository, questRepository, assignmentRepository);

        // Act & Assert
        assertThatThrownBy(() -> service.assign("m-1", "q-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Albéric");

        // aucune attribution supplementaire n'a ete enregistree
        assertThat(assignmentRepository.findByMember("m-1"))
                .extracting(assignment -> assignment.quest().id())
                .containsExactly("q-1");
    }

    // Chapitre 5 — « Attribuer une quête en TDD » (approche par les interactions)
    @Test
    void should_reject_assignment_when_a_prerequisite_quest_is_not_completed() {
        // Arrange
        Member alberic = new Member("m-1", "Albéric", MemberRank.NOVICE, 0, 5);
        Quest gated = new Quest("q-2", "Terrasser le dragon", QuestDifficulty.LEGENDARY, 500, 300, "q-1");
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-2")).thenReturn(Optional.of(gated));
        QuestAssignmentService service =
                new QuestAssignmentService(memberRepository, questRepository, assignmentRepository);

        // Act & Assert
        assertThatThrownBy(() -> service.assign("m-1", "q-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("q-1");

        assertThat(assignmentRepository.findByMember("m-1")).isEmpty();
    }

    @Test
    void should_assign_a_gated_quest_once_its_prerequisite_is_completed() {
        // Arrange
        Member alberic = new Member("m-1", "Albéric", MemberRank.NOVICE, 0, 5);
        Quest prerequisite = Quest.standalone("q-1", "Escorter la caravane", QuestDifficulty.MEDIUM, 120, 60);
        Quest gated = new Quest("q-2", "Terrasser le dragon", QuestDifficulty.LEGENDARY, 500, 300, "q-1");
        assignmentRepository.add(new QuestAssignment(alberic, prerequisite, QuestAssignmentStatus.COMPLETED));
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-2")).thenReturn(Optional.of(gated));
        QuestAssignmentService service =
                new QuestAssignmentService(memberRepository, questRepository, assignmentRepository);

        // Act
        QuestAssignment assignment = service.assign("m-1", "q-2");

        // Assert
        assertThat(assignment.quest()).isEqualTo(gated);
        assertThat(assignment.status()).isEqualTo(QuestAssignmentStatus.ASSIGNED);
    }

    @Test
    void should_reject_assignment_when_member_is_unknown() {
        // Arrange
        when(memberRepository.findById("m-404")).thenReturn(Optional.empty());
        QuestAssignmentService service =
                new QuestAssignmentService(memberRepository, questRepository, assignmentRepository);

        // Act & Assert
        assertThatThrownBy(() -> service.assign("m-404", "q-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("m-404");
    }

}
