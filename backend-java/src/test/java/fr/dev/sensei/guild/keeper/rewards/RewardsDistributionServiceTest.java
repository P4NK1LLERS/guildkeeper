package fr.dev.sensei.guild.keeper.rewards;

import fr.dev.sensei.guild.keeper.experience.ExperienceCalculator;
import fr.dev.sensei.guild.keeper.missions.LootCalculator;
import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.missions.QuestDifficulty;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RewardsDistributionServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private final FakeNotificationPort notificationPort = new FakeNotificationPort();
    private final ExperienceCalculator experienceCalculator = new ExperienceCalculator();
    private final LootCalculator lootCalculator = new LootCalculator();

    @Test
    void should_credit_experience_and_loot_when_quest_is_completed() {
        // Arrange
        Member veteran = new Member("m-1", "Dante", MemberRank.VETERAN, 0, 5);
        Quest quest = Quest.standalone("q-1", "Purger le donjon", QuestDifficulty.HARD, 100, 100);
        QuestAssignment assignment = new QuestAssignment(veteran, quest, QuestAssignmentStatus.COMPLETED);
        RewardsDistributionService service = new RewardsDistributionService(
                memberRepository, notificationPort, experienceCalculator, lootCalculator);

        // Act
        RewardsDistributionService.RewardsResult result = service.distributeRewards(assignment);

        // Assert
        assertThat(result.experienceGained()).isEqualTo(120);
        assertThat(result.lootValue()).isEqualTo(125);
        assertThat(veteran.experiencePoints()).isEqualTo(120);
        verify(memberRepository).save(veteran);
    }

    private RewardsDistributionService serviceUnderTest() {
        return new RewardsDistributionService(
                memberRepository, notificationPort, experienceCalculator, lootCalculator);
    }

    // Chapitre 4 — live coding « Isoler la distribution de récompenses, en Java puis en TypeScript »
    @Test
    void should_notify_member_after_distributing_rewards() {
        // Arrange
        Member dante = new Member("m-1", "Dante", MemberRank.VETERAN, 0, 5);
        Quest quest = Quest.standalone("q-1", "Purger le donjon", QuestDifficulty.HARD, 100, 100);
        QuestAssignment assignment = new QuestAssignment(dante, quest, QuestAssignmentStatus.COMPLETED);
        RewardsDistributionService service = serviceUnderTest();

        // Act
        service.distributeRewards(assignment);

        // Assert
        assertThat(notificationPort.count()).isEqualTo(1);
        assertThat(notificationPort.hasNotified(dante)).isTrue();
        assertThat(notificationPort.lastNotification().member()).isEqualTo(dante);
        assertThat(notificationPort.lastNotification().message())
                .contains("Purger le donjon")
                .contains("120")
                .contains("125");
    }

    // Chapitre 4 — « Atelier pratique - Isoler les dépendances externes de GuildKeeper »
    //       (test d'isolation : quête non COMPLETED -> exception levée et personne n'est notifié)
    @ParameterizedTest(name = "statut {0} -> aucune notification")
    @EnumSource(value = QuestAssignmentStatus.class, names = {"ASSIGNED", "ABANDONED"})
    void should_not_notify_anyone_when_quest_is_not_completed(QuestAssignmentStatus status) {
        // Arrange
        Member dante = new Member("m-1", "Dante", MemberRank.VETERAN, 0, 5);
        Quest quest = Quest.standalone("q-1", "Purger le donjon", QuestDifficulty.HARD, 100, 100);
        QuestAssignment assignment = new QuestAssignment(dante, quest, status);
        RewardsDistributionService service = serviceUnderTest();

        // Act & Assert
        assertThatThrownBy(() -> service.distributeRewards(assignment))
                .isInstanceOf(IllegalStateException.class);

        assertThat(notificationPort.count()).isZero();
        assertThat(dante.experiencePoints()).isZero();
        verify(memberRepository, never()).save(any());
    }

    @Test
    void should_credit_the_member_before_saving_and_notifying_him() {
        // Arrange
        Member dante = new Member("m-1", "Dante", MemberRank.VETERAN, 0, 5);
        Quest quest = Quest.standalone("q-1", "Purger le donjon", QuestDifficulty.HARD, 100, 100);
        QuestAssignment assignment = new QuestAssignment(dante, quest, QuestAssignmentStatus.COMPLETED);
        RewardsDistributionService service = serviceUnderTest();

        // Act
        service.distributeRewards(assignment);

        // Assert : le membre persiste puis notifie porte deja le gain d'experience
        verify(memberRepository).save(dante);
        assertThat(notificationPort.lastNotification().member().experiencePoints()).isEqualTo(120);
    }
}
