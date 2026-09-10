package fr.dev.sensei.guild.keeper.promotion;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberPromotionServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Test
    void should_promote_novice_to_apprentice_when_threshold_is_reached() {
        // Arrange
        Member dorian = new Member("m-1", "Dorian", MemberRank.NOVICE, 100, 4);
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(dorian));
        MemberPromotionService service = new MemberPromotionService(memberRepository);

        // Act
        Optional<MemberRank> newRank = service.promoteIfEligible("m-1");

        // Assert
        assertThat(newRank).contains(MemberRank.APPRENTICE);
        assertThat(dorian.rank()).isEqualTo(MemberRank.APPRENTICE);
    }

    // Chapitre 5 — « Atelier pratique - Promouvoir un membre de la guilde »
    @ParameterizedTest(name = "{0} + {1} XP -> {2}")
    @MethodSource("promotionThresholds")
    void should_promote_to_next_rank_when_threshold_is_reached(MemberRank startingRank, int experiencePoints, MemberRank expectedRank) {
        // Arrange
        Member dante = new Member("m-1", "Dante", startingRank, experiencePoints, 4);
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(dante));
        MemberPromotionService service = new MemberPromotionService(memberRepository);

        // Act
        Optional<MemberRank> newRank = service.promoteIfEligible("m-1");

        // Assert
        assertThat(newRank).contains(expectedRank);
        assertThat(dante.rank()).isEqualTo(expectedRank);
        verify(memberRepository).save(dante);
    }

    static Stream<Arguments> promotionThresholds() {
        return Stream.of(
                Arguments.of(MemberRank.NOVICE, 100, MemberRank.APPRENTICE),
                Arguments.of(MemberRank.APPRENTICE, 300, MemberRank.VETERAN),
                Arguments.of(MemberRank.VETERAN, 700, MemberRank.ELITE),
                Arguments.of(MemberRank.ELITE, 1500, MemberRank.GUILD_MASTER));
    }

    @ParameterizedTest(name = "{0} + {1} XP -> pas de promotion")
    @MethodSource("belowThresholds")
    void should_not_promote_when_the_next_threshold_is_not_reached(MemberRank startingRank, int experiencePoints) {
        // Arrange
        Member dante = new Member("m-1", "Dante", startingRank, experiencePoints, 4);
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(dante));
        MemberPromotionService service = new MemberPromotionService(memberRepository);

        // Act
        Optional<MemberRank> newRank = service.promoteIfEligible("m-1");

        // Assert
        assertThat(newRank).isEmpty();
        assertThat(dante.rank()).isEqualTo(startingRank);
        verify(memberRepository, never()).save(any());
    }

    static Stream<Arguments> belowThresholds() {
        // un XP en dessous de chaque seuil : la borne est bien un ">=" et pas un ">"
        return Stream.of(
                Arguments.of(MemberRank.NOVICE, 99),
                Arguments.of(MemberRank.APPRENTICE, 299),
                Arguments.of(MemberRank.VETERAN, 699),
                Arguments.of(MemberRank.ELITE, 1499));
    }

    @Test
    void should_promote_only_one_rank_at_a_time_even_when_experience_covers_several_steps() {
        // Arrange
        Member dante = new Member("m-1", "Dante", MemberRank.NOVICE, 5000, 4);
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(dante));
        MemberPromotionService service = new MemberPromotionService(memberRepository);

        // Act
        Optional<MemberRank> newRank = service.promoteIfEligible("m-1");

        // Assert
        assertThat(newRank).contains(MemberRank.APPRENTICE);
        assertThat(dante.rank()).isEqualTo(MemberRank.APPRENTICE);
    }

    // Chapitre 5 — « Atelier pratique - Promouvoir un membre de la guilde »
    @Test
    void should_not_promote_member_who_is_already_guild_master() {
        // Arrange
        Member ektor = new Member("m-1", "Ektor", MemberRank.GUILD_MASTER, 9000, 4);
        when(memberRepository.findById("m-1")).thenReturn(Optional.of(ektor));
        MemberPromotionService service = new MemberPromotionService(memberRepository);

        // Act
        Optional<MemberRank> newRank = service.promoteIfEligible("m-1");

        // Assert
        assertThat(newRank).isEmpty();
        assertThat(ektor.rank()).isEqualTo(MemberRank.GUILD_MASTER);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void should_reject_promotion_when_member_is_unknown() {
        // Arrange
        when(memberRepository.findById("m-404")).thenReturn(Optional.empty());
        MemberPromotionService service = new MemberPromotionService(memberRepository);

        // Act & Assert
        assertThatThrownBy(() -> service.promoteIfEligible("m-404"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("m-404");
    }
}
