package fr.dev.sensei.guild.keeper;

import fr.dev.sensei.guild.keeper.experience.ExperienceCalculator;
import fr.dev.sensei.guild.keeper.finance.GuildAccountRepository;
import fr.dev.sensei.guild.keeper.finance.GuildFinanceService;
import fr.dev.sensei.guild.keeper.missions.LootCalculator;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentRepository;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentService;
import fr.dev.sensei.guild.keeper.missions.QuestRepository;
import fr.dev.sensei.guild.keeper.persistence.sqlite.SqliteGuildAccountRepository;
import fr.dev.sensei.guild.keeper.persistence.sqlite.SqliteMemberRepository;
import fr.dev.sensei.guild.keeper.persistence.sqlite.SqliteQuestAssignmentRepository;
import fr.dev.sensei.guild.keeper.persistence.sqlite.SqliteQuestRepository;
import fr.dev.sensei.guild.keeper.promotion.MemberPromotionService;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;
import fr.dev.sensei.guild.keeper.recruitment.RecruitmentService;
import fr.dev.sensei.guild.keeper.rewards.NotificationPort;
import fr.dev.sensei.guild.keeper.rewards.RewardEstimator;
import fr.dev.sensei.guild.keeper.rewards.RewardsDistributionService;

import java.sql.Connection;

/**
 * Perimetre d'une unite de travail : repositories et services metier cables sur
 * une connexion SQLite donnee. Construit par {@link GuildKeeperModule} pour la
 * duree d'un appel.
 *
 * <p>Les services sont recrees a chaque acces ; ils sont tous sans etat.
 */
public final class GuildKeeperServices {

    private final String guildId;
    private final NotificationPort notificationPort;

    private final SqliteMemberRepository members;
    private final SqliteQuestRepository quests;
    private final SqliteGuildAccountRepository guildAccounts;
    private final SqliteQuestAssignmentRepository questAssignments;

    GuildKeeperServices(Connection connection, String guildId, NotificationPort notificationPort) {
        this.guildId = guildId;
        this.notificationPort = notificationPort;
        this.members = new SqliteMemberRepository(connection);
        this.quests = new SqliteQuestRepository(connection);
        this.guildAccounts = new SqliteGuildAccountRepository(connection);
        this.questAssignments = new SqliteQuestAssignmentRepository(connection, members, quests);
    }

    // ----- repositories -----

    public MemberRepository members() {
        return members;
    }

    public QuestRepository quests() {
        return quests;
    }

    public GuildAccountRepository guildAccounts() {
        return guildAccounts;
    }

    public QuestAssignmentRepository questAssignments() {
        return questAssignments;
    }

    // ----- services metier -----

    public RecruitmentService recruitment() {
        return new RecruitmentService(members);
    }

    public GuildFinanceService finance() {
        return new GuildFinanceService(guildAccounts);
    }

    public MemberPromotionService promotion() {
        return new MemberPromotionService(members);
    }

    public RewardsDistributionService rewards() {
        return new RewardsDistributionService(
                members, notificationPort, new ExperienceCalculator(), new LootCalculator());
    }

    public QuestAssignmentService questAssignmentService() {
        return new QuestAssignmentService(members, quests, questAssignments);
    }

    public RewardEstimator rewardEstimator() {
        return new RewardEstimator();
    }

    /** Identifiant logique du compte unique de la guilde (le schema n'en modelise qu'un). */
    public String guildId() {
        return guildId;
    }
}
