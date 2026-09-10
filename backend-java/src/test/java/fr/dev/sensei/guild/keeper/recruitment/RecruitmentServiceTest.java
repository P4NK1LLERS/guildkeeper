package fr.dev.sensei.guild.keeper.recruitment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RecruitmentService recruitmentService;

    @Test
    void should_recruit_candidate_when_name_is_valid_and_not_taken() {
        // Arrange
        when(memberRepository.findByName("Dorian")).thenReturn(Optional.empty());

        // Act
        Member recruit = recruitmentService.recruit("Dorian");

        // Assert
        assertThat(recruit.name()).isEqualTo("Dorian");
        assertThat(recruit.rank()).isEqualTo(MemberRank.NOVICE);
        assertThat(recruit.experiencePoints()).isZero();

        ArgumentCaptor<Member> savedMember = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(savedMember.capture());
        assertThat(savedMember.getValue().name()).isEqualTo("Dorian");
    }

    @Test
    void should_throw_DuplicateMemberException_when_name_already_exists() {
        // Arrange
        Member existingMember = Member.novice("id-1", "Dorian", 1);
        when(memberRepository.findByName("Dorian")).thenReturn(Optional.of(existingMember));

        // Act
        Throwable thrown = catchThrowable(() -> recruitmentService.recruit("Dorian"));

        // Assert
        assertThat(thrown)
                .isInstanceOf(DuplicateMemberException.class)
                .hasMessageContaining("Dorian");
        verify(memberRepository, never()).save(any());
    }

    // Chapitre 4 — « TP guidé - Isoler le service de recrutement »
    //       (Given/When/Then posés en Chapitre 1 — « Atelier pratique - Premiers pas sur GuildKeeper »)
    @ParameterizedTest(name = "nom vide [{0}] -> IllegalArgumentException")
    @ValueSource(strings = {"", " ", "   ", "	"})
    void should_reject_candidate_when_name_is_blank(String blankName) {
        // Act & Assert
        assertThatThrownBy(() -> recruitmentService.recruit(blankName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoire");

        // le repository n'est jamais sollicite : la validation coupe avant
        verify(memberRepository, never()).findByName(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void should_throw_IllegalArgumentException_when_name_is_null() {
        // Act & Assert
        assertThatThrownBy(() -> recruitmentService.recruit(null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    void should_trim_the_candidate_name_before_recruiting() {
        // Arrange
        when(memberRepository.findByName("Attila")).thenReturn(Optional.empty());

        // Act
        Member recruit = recruitmentService.recruit("  Attila  ");

        // Assert
        assertThat(recruit.name()).isEqualTo("Attila");
    }

    @Test
    void should_give_each_recruit_a_distinct_identifier() {
        // Arrange
        when(memberRepository.findByName(any())).thenReturn(Optional.empty());

        // Act
        Member attila = recruitmentService.recruit("Attila");
        Member dragan = recruitmentService.recruit("Dragan");

        // Assert
        assertThat(attila.id()).isNotBlank().isNotEqualTo(dragan.id());
    }
}
