    package fr.dev.sensei.guild.keeper.http;

import fr.dev.sensei.guild.keeper.DemoData;
import fr.dev.sensei.guild.keeper.GuildKeeperModule;
import fr.dev.sensei.guild.keeper.experience.QuestNotCompletedException;
import fr.dev.sensei.guild.keeper.finance.InsufficientFundsException;
import fr.dev.sensei.guild.keeper.finance.InvalidAmountException;
import fr.dev.sensei.guild.keeper.http.dto.ErrorDto;
import fr.dev.sensei.guild.keeper.recruitment.DuplicateMemberException;
import io.javalin.Javalin;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entree du serveur GuildKeeper.
 *
 * <p>Lance par {@code java -jar guildkeeper.jar} ou {@code mvn -q compile exec:java}.
 * Le port est celui de {@code GUILDKEEPER_PORT}, sinon {@link #DEFAULT_PORT}.
 *
 * <p>{@link #create(GuildKeeperModule)} rend l'instance Javalin non demarree : les
 * tests l'utilisent directement via {@code JavalinTest.test(...)}.
 */
public final class GuildKeeperServer {

    public static final int DEFAULT_PORT = 7070;
    private static final String PORT_ENV = "GUILDKEEPER_PORT";
    private static final Logger LOG = LoggerFactory.getLogger(GuildKeeperServer.class);

    private GuildKeeperServer() {
    }

    public static void main(String[] args) {
        int port = resolvePort();
        GuildKeeperModule module = GuildKeeperModule.fromEnvironment();
        DemoData.seedIfDevEnvironment(module);
        create(module).start(port);
        LOG.info("GuildKeeper demarre sur http://localhost:{}", port);
        LOG.info("Test rapide http://localhost:{}/api/v1/quests", port);
    }

    public static Javalin create(GuildKeeperModule module) {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false);
        registerErrorHandling(app);
        new QuestRoutes(module).register(app);
        new MemberRoutes(module).register(app);
        new GuildRoutes(module).register(app);
        return app;
    }

    private static void registerErrorHandling(Javalin app) {
        app.exception(ApiException.class, (e, ctx) ->
                ctx.status(e.status()).json(new ErrorDto(e.code(), e.getMessage())));

        // Exceptions metier -> codes stables (voir docs/architecture.md §5.4).
        app.exception(DuplicateMemberException.class, render(409, "CONFLICT"));
        app.exception(InsufficientFundsException.class, render(409, "CONFLICT"));
        app.exception(QuestNotCompletedException.class, render(409, "CONFLICT"));
        app.exception(InvalidAmountException.class, render(400, "VALIDATION"));
        app.exception(IllegalArgumentException.class, render(400, "VALIDATION"));
        app.exception(IllegalStateException.class, render(409, "CONFLICT"));

        // Erreurs levees par Javalin lui-meme (corps JSON illisible, etc.).
        app.exception(HttpResponseException.class, (e, ctx) ->
                ctx.status(e.getStatus()).json(new ErrorDto(codeForStatus(e.getStatus()), e.getMessage())));

        app.exception(Exception.class, (e, ctx) -> {
            LOG.error("Erreur non geree", e);
            ctx.status(500).json(new ErrorDto("INTERNAL", "Erreur interne du serveur."));
        });

        // Route non reconnue : ne rien ecraser si une reponse a deja ete produite.
        app.error(404, ctx -> {
            if (ctx.result() == null && ctx.resultInputStream() == null) {
                ctx.json(new ErrorDto("NOT_FOUND", "Route inconnue : " + ctx.method() + " " + ctx.path()));
            }
        });
    }

    private static <E extends Exception> ExceptionHandler<E> render(int status, String code) {
        return (e, ctx) -> ctx.status(status).json(new ErrorDto(code, e.getMessage()));
    }

    private static String codeForStatus(int status) {
        return switch (status) {
            case 400 -> "VALIDATION";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            default -> "INTERNAL";
        };
    }

    private static int resolvePort() {
        String raw = System.getenv(PORT_ENV);
        if (raw == null || raw.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(PORT_ENV + " invalide : " + raw);
        }
    }
}
