package fr.dev.sensei.guild.keeper.http;

import fr.dev.sensei.guild.keeper.GuildKeeperModule;
import fr.dev.sensei.guild.keeper.GuildKeeperServices;
import fr.dev.sensei.guild.keeper.http.dto.AssignQuestRequest;
import fr.dev.sensei.guild.keeper.http.dto.AssignmentDto;
import fr.dev.sensei.guild.keeper.http.dto.MemberDto;
import fr.dev.sensei.guild.keeper.http.dto.PromotionResultDto;
import fr.dev.sensei.guild.keeper.http.dto.RecruitMemberRequest;
import fr.dev.sensei.guild.keeper.http.dto.RewardsResultDto;
import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.rewards.RewardsDistributionService.RewardsResult;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Routes des membres : lecture, recrutement, assignation et completion de quete, promotion. */
public final class MemberRoutes {

    private final GuildKeeperModule module;

    public MemberRoutes(GuildKeeperModule module) {
        this.module = module;
    }

    public void register(Javalin app) {
        app.get("/api/v1/members", this::list);
        app.get("/api/v1/members/{name}", this::getOne);
        app.get("/api/v1/members/{name}/assignments", this::assignments);
        app.post("/api/v1/members", this::recruit);
        app.post("/api/v1/members/{name}/quests", this::assignQuest);
        app.post("/api/v1/members/{name}/quests/{id}/completion", this::completeQuest);
        app.post("/api/v1/members/{name}/promotion", this::promote);
    }

    private void list(Context ctx) {
        List<MemberDto> members = module.execute(services -> services.members().findAll().stream()
                .map(MemberDto::from)
                .sorted(Comparator.comparing(MemberDto::name))
                .toList());
        ctx.json(members);
    }

    private void getOne(Context ctx) {
        String name = ctx.pathParam("name");
        Member member = module.execute(services -> services.members().findByName(name))
                .orElseThrow(() -> ApiException.notFound("Aucun membre nomme " + name + " dans la guilde."));
        ctx.json(MemberDto.from(member));
    }

    private void assignments(Context ctx) {
        String name = ctx.pathParam("name");
        List<AssignmentDto> assignments = module.execute(services -> {
            Member member = requireMember(services, name);
            return services.questAssignments().findByMember(member.id()).stream()
                    .map(assignment -> new AssignmentDto(
                            assignment.quest().id(),
                            assignment.quest().title(),
                            assignment.status().name()))
                    .toList();
        });
        ctx.json(assignments);
    }

    private void recruit(Context ctx) {
        RecruitMemberRequest request = ctx.bodyAsClass(RecruitMemberRequest.class);
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw ApiException.validation("Le champ 'name' est obligatoire.");
        }
        Member member = module.execute(services -> services.recruitment().recruit(request.name().trim()));
        ctx.status(201).json(MemberDto.from(member));
    }

    private void assignQuest(Context ctx) {
        String name = ctx.pathParam("name");
        AssignQuestRequest request = ctx.bodyAsClass(AssignQuestRequest.class);
        if (request == null || request.questId() == null || request.questId().isBlank()) {
            throw ApiException.validation("Le champ 'questId' est obligatoire.");
        }
        AssignmentDto dto = module.execute(services -> {
            Member member = requireMember(services, name);
            Quest quest = requireQuest(services, request.questId());
            QuestAssignment assignment;
            try {
                assignment = services.questAssignmentService().assign(member.id(), quest.id());
            } catch (IllegalStateException e) {
                throw ApiException.conflict(readableAssignmentError(services, member, quest, e.getMessage()));
            }
            return new AssignmentDto(quest.id(), quest.title(), assignment.status().name());
        });
        ctx.status(201).json(dto);
    }

    private void completeQuest(Context ctx) {
        String name = ctx.pathParam("name");
        String questId = ctx.pathParam("id");
        RewardsResultDto result = module.execute(services -> {
            Member member = requireMember(services, name);
            Quest quest = requireQuest(services, questId);

            List<QuestAssignment> forThisQuest = services.questAssignments().findByMember(member.id()).stream()
                    .filter(assignment -> assignment.quest().id().equals(quest.id()))
                    .toList();
            if (forThisQuest.stream().anyMatch(a -> a.status() == QuestAssignmentStatus.COMPLETED)) {
                throw ApiException.conflict(
                        "La quete \"" + quest.title() + "\" est deja completee par " + member.name() + ".");
            }
            if (forThisQuest.stream().noneMatch(a -> a.status() == QuestAssignmentStatus.ASSIGNED)) {
                throw ApiException.conflict(
                        member.name() + " n'a pas la quete \"" + quest.title() + "\" en cours.");
            }

            RewardsResult rewards = services.rewards().distributeRewards(
                    new QuestAssignment(member, quest, QuestAssignmentStatus.COMPLETED));
            services.questAssignments().markCompleted(member.id(), quest.id());

            Member updated = services.members().findByName(name).orElse(member);
            return RewardsResultDto.of(rewards, updated);
        });
        ctx.json(result);
    }

    private void promote(Context ctx) {
        String name = ctx.pathParam("name");
        PromotionResultDto result = module.execute(services -> {
            Member member = requireMember(services, name);
            String previousRank = member.rank().name();
            Optional<MemberRank> newRank = services.promotion().promoteIfEligible(member.id());
            Member updated = services.members().findByName(name).orElse(member);
            return PromotionResultDto.of(newRank.isPresent(), previousRank, updated);
        });
        ctx.json(result);
    }

    private static Member requireMember(GuildKeeperServices services, String name) {
        return services.members().findByName(name)
                .orElseThrow(() -> ApiException.notFound("Aucun membre nomme " + name + " dans la guilde."));
    }

    private static Quest requireQuest(GuildKeeperServices services, String questId) {
        return services.quests().findById(questId)
                .orElseThrow(() -> ApiException.notFound("Aucune quete #" + questId + " au catalogue."));
    }

    /** Traduit le message technique de {@link fr.dev.sensei.guild.keeper.missions.QuestAssignmentService}. */
    private static String readableAssignmentError(GuildKeeperServices services, Member member, Quest quest,
                                                  String fallback) {
        if (quest.hasPrerequisite()) {
            boolean done = services.questAssignments().findByMember(member.id()).stream()
                    .anyMatch(a -> a.quest().id().equals(quest.prerequisiteQuestId())
                            && a.status() == QuestAssignmentStatus.COMPLETED);
            if (!done) {
                String title = services.quests().findById(quest.prerequisiteQuestId())
                        .map(Quest::title)
                        .orElse("#" + quest.prerequisiteQuestId());
                return "Le prerequis \"" + title + "\" n'est pas encore complete par " + member.name() + ".";
            }
        }
        return fallback;
    }
}
