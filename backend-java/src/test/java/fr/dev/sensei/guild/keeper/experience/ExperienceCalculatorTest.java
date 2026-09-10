package fr.dev.sensei.guild.keeper.experience;

import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.missions.QuestDifficulty;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExperienceCalculatorTest {

    private final ExperienceCalculator calculator = new ExperienceCalculator();

    private static QuestAssignment completedQuestFor(MemberRank rank) {
        Member member = new Member("m-" + rank, "Dragan", rank, 0, 5);
        Quest quest = Quest.standalone("q-1", "Nettoyer les caves", QuestDifficulty.EASY, 100, 40);
        return new QuestAssignment(member, quest, QuestAssignmentStatus.COMPLETED);
    }

    private static QuestAssignment completedLegendaryQuestFor(MemberRank rank) {
        Member member = new Member("m-" + rank, "Ektor", rank, 0, 5);
        Quest quest = Quest.standalone("q-1", "Nettoyer les caves", QuestDifficulty.LEGENDARY, 100, 40);
        return new QuestAssignment(member, quest, QuestAssignmentStatus.COMPLETED);
    }

    private static QuestAssignment assignmentWithStatus(MemberRank rank, QuestAssignmentStatus status) {
        Member member = new Member("m-" + rank, "Dragan", rank, 0, 5);
        Quest quest = Quest.standalone("q-1", "Nettoyer les caves", QuestDifficulty.EASY, 100, 40);
        return new QuestAssignment(member, quest, status);
    }

    @Test
    void should_grant_base_experience_when_member_is_novice() {
        // Arrange
        QuestAssignment assignment = completedQuestFor(MemberRank.NOVICE);

        // Act
        int reward = calculator.calculateExperienceReward(assignment);

        // Assert
        assertThat(reward).isEqualTo(100);
    }

    @Test
    void should_grant_10_percent_bonus_when_member_is_apprentice() {
        // Arrange
        QuestAssignment assignment = completedQuestFor(MemberRank.APPRENTICE);

        // Act
        int reward = calculator.calculateExperienceReward(assignment);

        // Assert
        assertThat(reward).isEqualTo(110);
    }

    @Test
    void should_grant_20_percent_bonus_when_member_is_veteran() {
        // Arrange
        QuestAssignment assignment = completedQuestFor(MemberRank.VETERAN);

        // Act
        int reward = calculator.calculateExperienceReward(assignment);

        // Assert
        assertThat(reward).isEqualTo(120);
    }

    @Test
    void should_grant_30_percent_bonus_when_member_is_elite() {
        // Arrange
        QuestAssignment assignment = completedQuestFor(MemberRank.ELITE);

        // Act
        int reward = calculator.calculateExperienceReward(assignment);

        // Assert
        assertThat(reward).isEqualTo(130);
    }

    @Test
    void should_add_fixed_50_xp_boost_when_legendary_quest_completed_by_novice() {
        // Arrange
        QuestAssignment assignment = completedLegendaryQuestFor(MemberRank.NOVICE);

        // Act
        int reward = calculator.calculateExperienceReward(assignment);

        // Assert
        assertThat(reward).isEqualTo(150);
    }

    @Test
    void should_not_add_the_legendary_boost_when_member_is_not_novice() {
        // Arrange
        QuestAssignment assignment = completedLegendaryQuestFor(MemberRank.APPRENTICE);

        // Act
        int reward = calculator.calculateExperienceReward(assignment);

        // Assert
        assertThat(reward).isEqualTo(110);
    }

    @ParameterizedTest(name = "statut {0} -> QuestNotCompletedException")
    @EnumSource(value = QuestAssignmentStatus.class, names = {"ASSIGNED", "ABANDONED"})
    void should_throw_business_exception_when_quest_is_not_completed(QuestAssignmentStatus status) {
        // Arrange
        QuestAssignment assignment = assignmentWithStatus(MemberRank.NOVICE, status);

        // Act & Assert
        assertThatThrownBy(() -> calculator.calculateExperienceReward(assignment))
                .isInstanceOf(QuestNotCompletedException.class)
                .hasMessageContaining("q-1")
                .hasMessageContaining(status.name());
    }
}
