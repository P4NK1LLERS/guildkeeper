package fr.dev.sensei.guild.keeper.http;

import fr.dev.sensei.guild.keeper.GuildKeeperModule;
import fr.dev.sensei.guild.keeper.http.dto.QuestDto;
import fr.dev.sensei.guild.keeper.http.dto.RewardPreviewDto;
import fr.dev.sensei.guild.keeper.missions.Quest;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

/** Routes de lecture du catalogue de quetes. */
public final class QuestRoutes {

    private final GuildKeeperModule module;

    public QuestRoutes(GuildKeeperModule module) {
        this.module = module;
    }

    public void register(Javalin app) {
        app.get("/api/v1/quests", this::list);
        app.get("/api/v1/quests/{id}", this::getOne);
        app.get("/api/v1/quests/{id}/reward-preview", this::rewardPreview);
    }

    private void list(Context ctx) {
        List<QuestDto> quests = module.execute(services -> services.quests().findAll().stream()
                .map(QuestDto::from)
                .toList());
        ctx.json(quests);
    }

    private void getOne(Context ctx) {
        String id = ctx.pathParam("id");
        Quest quest = module.execute(services -> services.quests().findById(id))
                .orElseThrow(() -> ApiException.notFound("Aucune quete #" + id + " au catalogue."));
        ctx.json(QuestDto.from(quest));
    }

    private void rewardPreview(Context ctx) {
        String id = ctx.pathParam("id");
        int luck = ctx.queryParamAsClass("luck", Integer.class)
                .getOrThrow(ignored -> ApiException.validation("Le parametre 'luck' (entier) est obligatoire."));
        RewardPreviewDto dto = module.execute(services -> {
            Quest quest = services.quests().findById(id)
                    .orElseThrow(() -> ApiException.notFound("Aucune quete #" + id + " au catalogue."));
            return RewardPreviewDto.of(quest.id(), luck, services.rewardEstimator().estimateForNovice(quest, luck));
        });
        ctx.json(dto);
    }
}
