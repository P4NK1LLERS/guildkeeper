package fr.dev.sensei.guild.keeper.finance;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operations financieres sur le compte d'une guilde.
 *
 * <p>Perimetre : depot, distribution de butin, verification de solvabilite, et
 * distribution de dividendes par rang ({@link #distributeDividends}).
 */
public class GuildFinanceService {

    private final GuildAccountRepository guildAccountRepository;

    public GuildFinanceService(GuildAccountRepository guildAccountRepository) {
        this.guildAccountRepository = guildAccountRepository;
    }

    /**
     * Credite le compte de la guilde.
     *
     * @throws InvalidAmountException si {@code amount <= 0}
     */
    public void deposit(GuildAccount account, int amount) {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }
        account.increaseBy(amount);
        guildAccountRepository.save(account);
    }

    /**
     * Debite le compte du montant de butin distribue a un membre.
     *
     * @throws InsufficientFundsException si le compte n'est pas solvable pour ce montant
     */
    public void distributeLoot(GuildAccount account, int amount) {
        if (!checkSolvency(account, amount)) {
            throw new InsufficientFundsException(account.balance(), amount);
        }
        account.decreaseBy(amount);
        guildAccountRepository.save(account);
    }

    /** @return {@code true} si le solde couvre {@code amount}. */
    public boolean checkSolvency(GuildAccount account, int amount) {
        return account.balance() >= amount;
    }

    /**
     * Distribue une part du solde de la guilde aux membres, proportionnellement
     * au poids de leur rang (NOVICE=1, APPRENTICE=2, VETERAN=3, ELITE=4,
     * GUILD_MASTER=5). Guilde sans membre -> rien a distribuer, le compte n'est
     * pas touche. Le reliquat issu des arrondis par defaut n'est distribue a
     * personne : il reste sur le compte.
     *
     * @throws InvalidAmountException si {@code percentage <= 0} (pas de borne haute)
     * @throws InsufficientFundsException si le compte ne peut pas supporter la
     *     somme des parts calculees
     */
    public Map<Member, Integer> distributeDividends(GuildAccount account, List<Member> members, int percentage) {
        if (percentage <= 0) {
            throw new InvalidAmountException(percentage);
        }
        if (members.isEmpty()) {
            return Map.of();
        }
        int envelope = account.balance() * percentage / 100;
        int totalWeight = members.stream().mapToInt(member -> weightOf(member.rank())).sum();

        Map<Member, Integer> shares = new LinkedHashMap<>();
        int totalShares = 0;
        for (Member member : members) {
            int share = envelope * weightOf(member.rank()) / totalWeight;
            shares.put(member, share);
            totalShares += share;
        }

        if (!checkSolvency(account, totalShares)) {
            throw new InsufficientFundsException(account.balance(), totalShares);
        }

        account.decreaseBy(totalShares);
        guildAccountRepository.save(account);
        return shares;
    }

    /**
     * Poids d'un rang pour la distribution de dividendes : NOVICE=1 ...
     * GUILD_MASTER=5. S'appuie sur l'ordre de declaration de {@link MemberRank},
     * qui porte deja ce sens metier (voir sa javadoc).
     */
    private static int weightOf(MemberRank rank) {
        return rank.ordinal() + 1;
    }
}
