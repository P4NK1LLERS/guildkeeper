package fr.dev.sensei.guild.keeper.http;

import fr.dev.sensei.guild.keeper.GuildKeeperModule;
import fr.dev.sensei.guild.keeper.GuildKeeperServices;
import fr.dev.sensei.guild.keeper.finance.GuildAccount;
import fr.dev.sensei.guild.keeper.http.dto.AmountRequest;
import fr.dev.sensei.guild.keeper.http.dto.GuildStatusDto;
import io.javalin.Javalin;
import io.javalin.http.Context;

/** Routes de la guilde : etat de synthese, depot, distribution de butin. */
public final class GuildRoutes {

    private final GuildKeeperModule module;

    public GuildRoutes(GuildKeeperModule module) {
        this.module = module;
    }

    public void register(Javalin app) {
        app.get("/api/v1/guild", this::status);
        app.post("/api/v1/guild/deposits", this::deposit);
        app.post("/api/v1/guild/loot-distributions", this::distributeLoot);
    }

    private void status(Context ctx) {
        ctx.json(module.execute(this::currentStatus));
    }

    private void deposit(Context ctx) {
        int amount = requireAmount(ctx);
        GuildStatusDto dto = module.execute(services -> {
            services.finance().deposit(account(services), amount);
            return currentStatus(services);
        });
        ctx.json(dto);
    }

    private void distributeLoot(Context ctx) {
        int amount = requireAmount(ctx);
        GuildStatusDto dto = module.execute(services -> {
            services.finance().distributeLoot(account(services), amount);
            return currentStatus(services);
        });
        ctx.json(dto);
    }

    private GuildStatusDto currentStatus(GuildKeeperServices services) {
        int balance = account(services).balance();
        int memberCount = services.members().findAll().size();
        return new GuildStatusDto(balance, memberCount);
    }

    private static GuildAccount account(GuildKeeperServices services) {
        return services.guildAccounts().findByGuildId(services.guildId())
                .orElseThrow(() -> ApiException.internal("Compte de guilde introuvable."));
    }

    private static int requireAmount(Context ctx) {
        AmountRequest request = ctx.bodyAsClass(AmountRequest.class);
        if (request == null || request.amount() == null) {
            throw ApiException.validation("Le champ 'amount' est obligatoire.");
        }
        return request.amount();
    }
}
