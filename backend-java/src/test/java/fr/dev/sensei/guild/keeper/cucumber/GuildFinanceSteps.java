package fr.dev.sensei.guild.keeper.cucumber;

import fr.dev.sensei.guild.keeper.finance.GuildAccount;
import fr.dev.sensei.guild.keeper.finance.GuildAccountRepository;
import fr.dev.sensei.guild.keeper.finance.GuildFinanceService;
import fr.dev.sensei.guild.keeper.finance.InMemoryGuildAccountRepository;
import fr.dev.sensei.guild.keeper.finance.InsufficientFundsException;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Step definitions de {@code features/finance.feature}.
 *
 * <p>Modele : {@link RecruitmentSteps}. Reutilise {@link GuildFinanceService} et
 * {@link InMemoryGuildAccountRepository} : aucune regle metier n'est reimplementee
 * ici, les steps se contentent d'orchestrer le service reel.
 *
 * <p>Une nouvelle instance est creee par Cucumber pour chaque scenario : les
 * champs ci-dessous forment donc l'etat isole d'un scenario.
 */
public class GuildFinanceSteps {

    private static final String GUILD_ID = "guilde-du-scenario";

    private GuildAccountRepository guildAccountRepository;
    private GuildFinanceService guildFinanceService;
    private GuildAccount account;
    private Throwable caughtException;

    @Soit("un compte de guilde avec un solde de {int} pièces d'or")
    public void un_compte_de_guilde_avec_un_solde_de_pieces_d_or(int soldeInitial) {
        guildAccountRepository = new InMemoryGuildAccountRepository();
        guildFinanceService = new GuildFinanceService(guildAccountRepository);
        account = new GuildAccount(GUILD_ID, soldeInitial);
        guildAccountRepository.save(account);
        caughtException = null;
    }

    @Quand("je distribue {int} pièces d'or de butin à {string}")
    public void je_distribue_pieces_d_or_de_butin_a(int montant, String membre) {
        guildFinanceService.distributeLoot(account, montant);
    }

    @Quand("j'essaie de distribuer {int} pièces d'or de butin à {string}")
    public void j_essaie_de_distribuer_pieces_d_or_de_butin_a(int montant, String membre) {
        caughtException = catchThrowable(() -> guildFinanceService.distributeLoot(account, montant));
    }

    @Alors("le compte de la guilde a un solde de {int} pièces d'or")
    public void le_compte_de_la_guilde_a_un_solde_de_pieces_d_or(int soldeAttendu) {
        assertThat(account.balance()).isEqualTo(soldeAttendu);
    }

    @Alors("la distribution est rejetée pour cause de solde insuffisant")
    public void la_distribution_est_rejetee_pour_cause_de_solde_insuffisant() {
        assertThat(caughtException).isInstanceOf(InsufficientFundsException.class);
    }
}
