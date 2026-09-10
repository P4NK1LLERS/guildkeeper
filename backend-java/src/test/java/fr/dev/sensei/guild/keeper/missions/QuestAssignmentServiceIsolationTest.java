package fr.dev.sensei.guild.keeper.missions;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests d'isolation du {@link QuestAssignmentService}.
 *
 * <p>Complementaire de {@code QuestAssignmentServiceTest}, qui verifie les
 * regles metier contre un {@code InMemoryQuestAssignmentRepository} reel (test
 * sociable). Ici les <em>trois</em> collaborateurs sont des mocks : on ne verifie
 * plus l'etat du repository mais les interactions du service avec ses ports —
 * quelles lectures il fait, dans quel ordre, et ce qu'il ecrit exactement.
 */
@ExtendWith(MockitoExtension.class)
class QuestAssignmentServiceIsolationTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestAssignmentRepository assignmentRepository;

    @InjectMocks
    private QuestAssignmentService service;

    private final Member alberic = new Member("m-1", "Albéric", MemberRank.NOVICE, 0, 5);
    private final Quest caravan =
            Quest.standalone("q-1", "Escorter la caravane", QuestDifficulty.MEDIUM, 120, 60);

    @Test
    void should_hand_the_repository_an_assignment_built_from_what_it_has_read() {
        // Arrange
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-1")).thenReturn(Optional.of(caravan));
        when(assignmentRepository.findByMember("m-1")).thenReturn(List.of());
        when(assignmentRepository.add(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        QuestAssignment assignment = service.assign("m-1", "q-1");

        // Assert : l'attribution ecrite porte bien le membre et la quete relus
        ArgumentCaptor<QuestAssignment> added = ArgumentCaptor.forClass(QuestAssignment.class);
        verify(assignmentRepository).add(added.capture());
        assertThat(added.getValue().member()).isSameAs(alberic);
        assertThat(added.getValue().quest()).isEqualTo(caravan);
        assertThat(added.getValue().status()).isEqualTo(QuestAssignmentStatus.ASSIGNED);

        // le service retourne ce que le repository lui rend, il ne fabrique pas sa propre reponse
        assertThat(assignment).isSameAs(added.getValue());
    }

    @Test
    void should_read_its_collaborators_in_order_before_writing() {
        // Arrange
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-1")).thenReturn(Optional.of(caravan));
        when(assignmentRepository.findByMember("m-1")).thenReturn(List.of());

        // Act
        service.assign("m-1", "q-1");

        // Assert : membre -> quete -> attributions existantes -> ecriture
        InOrder inOrder = inOrder(memberRepository, questRepository, assignmentRepository);
        inOrder.verify(memberRepository).findById("m-1");
        inOrder.verify(questRepository).findById("q-1");
        inOrder.verify(assignmentRepository).findByMember("m-1");
        inOrder.verify(assignmentRepository).add(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void should_stop_before_reading_the_quest_when_the_member_is_unknown() {
        // Arrange
        when(memberRepository.findById("m-404")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.assign("m-404", "q-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("m-404");

        // aucun autre port n'est sollicite : le service echoue au plus tot
        verifyNoInteractions(questRepository, assignmentRepository);
    }

    @Test
    void should_stop_before_reading_the_assignments_when_the_quest_is_unknown() {
        // Arrange
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-404")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.assign("m-1", "q-404"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("q-404");

        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void should_not_write_anything_when_the_member_already_has_a_quest_in_progress() {
        // Arrange
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-2")).thenReturn(Optional.of(
                Quest.standalone("q-2", "Purger le donjon", QuestDifficulty.HARD, 200, 100)));
        when(assignmentRepository.findByMember("m-1"))
                .thenReturn(List.of(new QuestAssignment(alberic, caravan, QuestAssignmentStatus.ASSIGNED)));

        // Act & Assert
        assertThatThrownBy(() -> service.assign("m-1", "q-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Albéric");

        verify(assignmentRepository, never()).add(any());
    }

    @Test
    void should_not_write_anything_when_the_prerequisite_is_only_assigned() {
        // Arrange : le prerequis est en cours, pas complete
        Quest gated = new Quest("q-2", "Terrasser le dragon", QuestDifficulty.LEGENDARY, 500, 300, "q-1");
        Member dragan = new Member("m-2", "Dragan", MemberRank.VETERAN, 400, 5);
        when(memberRepository.findById("m-2")).thenReturn(Optional.of(dragan));
        when(questRepository.findById("q-2")).thenReturn(Optional.of(gated));
        when(assignmentRepository.findByMember("m-2"))
                .thenReturn(List.of(new QuestAssignment(dragan, caravan, QuestAssignmentStatus.ABANDONED)));

        // Act & Assert
        assertThatThrownBy(() -> service.assign("m-2", "q-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("q-1");

        verify(assignmentRepository, never()).add(any());
    }

    @Test
    void should_reread_the_assignments_at_each_call_because_the_service_is_stateless() {
        // Arrange : le repository repond d'abord "aucune attribution", puis "une en cours"
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(alberic));
        when(questRepository.findById("q-1")).thenReturn(Optional.of(caravan));
        when(assignmentRepository.findByMember("m-1"))
                .thenReturn(List.of())
                .thenReturn(List.of(new QuestAssignment(alberic, caravan, QuestAssignmentStatus.ASSIGNED)));

        // Act
        service.assign("m-1", "q-1");

        // Assert : le second appel voit la nouvelle reponse du port, rien n'a ete memorise
        assertThatThrownBy(() -> service.assign("m-1", "q-1"))
                .isInstanceOf(IllegalStateException.class);
        verify(assignmentRepository, times(2)).findByMember("m-1");
        verify(assignmentRepository, times(1)).add(any());
    }
}
