package fr.dev.sensei.guild.keeper.finance;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests d'isolation du {@link GuildFinanceService}.
 *
 * <p>La seule dependance du service — le {@link GuildAccountRepository} — est
 * remplacee par un mock Mockito : aucune persistance reelle n'est sollicitee.
 * Deux choses distinctes sont verifiees :
 * <ul>
 *   <li>l'<em>etat</em> : le solde du {@link GuildAccount} apres l'operation ;</li>
 *   <li>les <em>interactions</em> : le repository est appele (ou non) au bon
 *       moment, avec le bon compte.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class GuildFinanceServiceTest {

    @Mock
    private GuildAccountRepository guildAccountRepository;

    @InjectMocks
    private GuildFinanceService financeService;

    private static GuildAccount accountWith(int balance) {
        return new GuildAccount("g-1", balance);
    }

    @Nested
    @DisplayName("deposit")
    class Deposit {

        @Test
        void should_credit_the_account_and_persist_it() {
            // Arrange
            GuildAccount account = accountWith(100);

            // Act
            financeService.deposit(account, 50);

            // Assert : etat
            assertThat(account.balance()).isEqualTo(150);

            // Assert : interaction — c'est bien CE compte qui est persiste
            ArgumentCaptor<GuildAccount> saved = ArgumentCaptor.forClass(GuildAccount.class);
            verify(guildAccountRepository).save(saved.capture());
            assertThat(saved.getValue()).isSameAs(account);
            verifyNoMoreInteractions(guildAccountRepository);
        }

        @ParameterizedTest(name = "montant {0} -> InvalidAmountException")
        @ValueSource(ints = {0, -1, -50, Integer.MIN_VALUE})
        void should_reject_non_positive_amounts_without_touching_the_repository(int invalidAmount) {
            // Arrange
            GuildAccount account = accountWith(100);

            // Act & Assert
            assertThatThrownBy(() -> financeService.deposit(account, invalidAmount))
                    .isInstanceOf(InvalidAmountException.class)
                    .hasMessageContaining(String.valueOf(invalidAmount));

            // le compte n'a pas bouge et la validation coupe AVANT la persistance
            assertThat(account.balance()).isEqualTo(100);
            verifyNoInteractions(guildAccountRepository);
        }

        @Test
        void should_credit_the_account_before_saving_it() {
            // Arrange : on capture le solde au moment precis de l'appel a save(...)
            GuildAccount account = accountWith(100);
            AtomicInteger balanceSeenBySave = new AtomicInteger();
            doAnswer(invocation -> {
                balanceSeenBySave.set(invocation.<GuildAccount>getArgument(0).balance());
                return null;
            }).when(guildAccountRepository).save(any());

            // Act
            financeService.deposit(account, 50);

            // Assert : le repository recoit un compte deja credite, pas l'ancien solde
            assertThat(balanceSeenBySave).hasValue(150);
        }

        @Test
        void should_propagate_the_repository_failure() {
            // Arrange
            GuildAccount account = accountWith(100);
            doThrow(new IllegalStateException("base indisponible"))
                    .when(guildAccountRepository).save(any());

            // Act & Assert : le service n'avale pas la panne de son adaptateur
            assertThatThrownBy(() -> financeService.deposit(account, 50))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("base indisponible");
        }
    }

    @Nested
    @DisplayName("distributeLoot")
    class DistributeLoot {

        @Test
        void should_debit_the_account_and_persist_it() {
            // Arrange
            GuildAccount account = accountWith(100);

            // Act
            financeService.distributeLoot(account, 30);

            // Assert
            assertThat(account.balance()).isEqualTo(70);
            verify(guildAccountRepository).save(account);
            verifyNoMoreInteractions(guildAccountRepository);
        }

        @Test
        void should_allow_a_distribution_that_empties_the_account() {
            // Arrange : borne exacte, checkSolvency est un ">=" et non un ">"
            GuildAccount account = accountWith(100);

            // Act
            financeService.distributeLoot(account, 100);

            // Assert
            assertThat(account.balance()).isZero();
            verify(guildAccountRepository).save(account);
        }

        @Test
        void should_reject_a_distribution_above_the_balance_without_saving() {
            // Arrange
            GuildAccount account = accountWith(100);

            // Act & Assert
            assertThatThrownBy(() -> financeService.distributeLoot(account, 101))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("100")
                    .hasMessageContaining("101");

            // le solde ne devient jamais negatif et rien n'est persiste
            assertThat(account.balance()).isEqualTo(100);
            verify(guildAccountRepository, never()).save(any());
        }

        @Test
        void should_debit_the_account_before_saving_it() {
            // Arrange
            GuildAccount account = accountWith(100);
            AtomicInteger balanceSeenBySave = new AtomicInteger();
            doAnswer(invocation -> {
                balanceSeenBySave.set(invocation.<GuildAccount>getArgument(0).balance());
                return null;
            }).when(guildAccountRepository).save(any());

            // Act
            financeService.distributeLoot(account, 30);

            // Assert
            assertThat(balanceSeenBySave).hasValue(70);
        }
    }

    @Nested
    @DisplayName("checkSolvency")
    class CheckSolvency {

        @ParameterizedTest(name = "solde 100, montant {0} -> solvable")
        @ValueSource(ints = {0, 1, 99, 100})
        void should_be_solvent_up_to_the_current_balance(int amount) {
            assertThat(financeService.checkSolvency(accountWith(100), amount)).isTrue();
        }

        @ParameterizedTest(name = "solde 100, montant {0} -> insolvable")
        @ValueSource(ints = {101, 200, Integer.MAX_VALUE})
        void should_not_be_solvent_above_the_current_balance(int amount) {
            assertThat(financeService.checkSolvency(accountWith(100), amount)).isFalse();
        }

        @Test
        void should_be_a_pure_query_that_never_touches_the_repository() {
            // Arrange
            GuildAccount account = accountWith(100);

            // Act
            financeService.checkSolvency(account, 100);

            // Assert : une requete ne modifie ni le solde ni l'etat persiste (CQS)
            assertThat(account.balance()).isEqualTo(100);
            verifyNoInteractions(guildAccountRepository);
        }
    }

    @Nested
    @DisplayName("distributeDividends")
    class DistributeDividends {

        @Test
        void should_distribute_nothing_when_guild_has_no_members() {
            // Arrange
            GuildAccount account = accountWith(1_000);

            // Act
            Map<Member, Integer> shares = financeService.distributeDividends(account, List.of(), 10);

            // Assert : rien a distribuer, le compte n'est pas touche
            assertThat(shares).isEmpty();
            assertThat(account.balance()).isEqualTo(1_000);
        }

        @Test
        void should_give_the_whole_envelope_to_the_only_member() {
            // Arrange : enveloppe = floor(1000 * 10 / 100) = 100
            GuildAccount account = accountWith(1_000);
            Member member = Member.novice("m-1", "Dragan", 5);

            // Act
            Map<Member, Integer> shares = financeService.distributeDividends(account, List.of(member), 10);

            // Assert : seul membre -> toute l'enveloppe, compte debite d'autant
            assertThat(shares).containsEntry(member, 100);
            assertThat(account.balance()).isEqualTo(900);
        }

        @Test
        void should_split_the_envelope_by_rank_weight_and_leave_the_remainder_on_the_account() {
            // Arrange : enveloppe = floor(1000*10/100) = 100, poids NOVICE=1 et
            // APPRENTICE=2 (total 3) -> parts floor(100*1/3)=33 et floor(100*2/3)=66,
            // reliquat 100-33-66=1 non distribue
            GuildAccount account = accountWith(1_000);
            Member novice = Member.novice("m-1", "Dragan", 5);
            Member apprentice = new Member("m-2", "Attila", MemberRank.APPRENTICE, 0, 5);

            // Act
            Map<Member, Integer> shares = financeService.distributeDividends(account, List.of(novice, apprentice), 10);

            // Assert : parts au prorata des poids
            assertThat(shares).containsEntry(novice, 33);
            assertThat(shares).containsEntry(apprentice, 66);

            // Assert : le reliquat reste sur le compte (on ne debite que 33+66=99)
            assertThat(account.balance()).isEqualTo(901);
        }

        @ParameterizedTest(name = "pourcentage {0} -> InvalidAmountException")
        @ValueSource(ints = {0, -5})
        void should_reject_a_non_positive_percentage_without_touching_the_repository(int invalidPercentage) {
            // Arrange
            GuildAccount account = accountWith(1_000);
            Member member = Member.novice("m-1", "Dragan", 5);

            // Act & Assert
            assertThatThrownBy(() -> financeService.distributeDividends(account, List.of(member), invalidPercentage))
                    .isInstanceOf(InvalidAmountException.class)
                    .hasMessageContaining(String.valueOf(invalidPercentage));

            // le compte n'a pas bouge et la validation coupe avant toute interaction
            assertThat(account.balance()).isEqualTo(1_000);
            verifyNoInteractions(guildAccountRepository);
        }

        @Test
        void should_reject_a_distribution_the_account_cannot_afford_and_leave_it_untouched() {
            // Arrange : enveloppe = floor(100*200/100) = 200, superieure au solde
            GuildAccount account = accountWith(100);
            Member member = Member.novice("m-1", "Dragan", 5);

            // Act & Assert
            assertThatThrownBy(() -> financeService.distributeDividends(account, List.of(member), 200))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("100")
                    .hasMessageContaining("200");

            // Assert : rien debite, rien persiste
            assertThat(account.balance()).isEqualTo(100);
            verify(guildAccountRepository, never()).save(any());
        }

        @Test
        void should_never_let_the_balance_go_negative_even_when_the_whole_balance_is_distributed() {
            // Arrange : enveloppe = floor(100*100/100) = 100 (tout le solde), 3 rangs
            // differents (poids 1+2+3=6) -> parts 16, 33, 50 (somme 99), reliquat 1
            GuildAccount account = accountWith(100);
            Member novice = Member.novice("m-1", "Dragan", 5);
            Member apprentice = new Member("m-2", "Attila", MemberRank.APPRENTICE, 0, 5);
            Member veteran = new Member("m-3", "Dorian", MemberRank.VETERAN, 0, 5);

            // Act
            Map<Member, Integer> shares =
                    financeService.distributeDividends(account, List.of(novice, apprentice, veteran), 100);

            // Assert : les arrondis par defaut ne font jamais deborder le solde disponible
            int totalDistributed = shares.values().stream().mapToInt(Integer::intValue).sum();
            assertThat(totalDistributed).isLessThanOrEqualTo(100);
            assertThat(account.balance()).isEqualTo(1);
            assertThat(account.balance()).isNotNegative();
        }
    }

    @Test
    void should_persist_the_account_once_per_successful_operation_and_in_order() {
        // Arrange
        GuildAccount account = accountWith(100);

        // Act
        financeService.deposit(account, 50);
        financeService.distributeLoot(account, 20);

        // Assert : etat final + sequence des appels au repository
        assertThat(account.balance()).isEqualTo(130);
        InOrder inOrder = inOrder(guildAccountRepository);
        inOrder.verify(guildAccountRepository, times(2)).save(account);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void should_never_reload_the_account_since_it_is_given_by_the_caller() {
        // Arrange
        GuildAccount account = accountWith(100);

        // Act
        financeService.deposit(account, 10);
        financeService.distributeLoot(account, 10);
        financeService.checkSolvency(account, 10);

        // Assert : le service travaille sur l'agregat recu, il ne le recharge jamais
        verify(guildAccountRepository, never()).findByGuildId(any());
    }
}
