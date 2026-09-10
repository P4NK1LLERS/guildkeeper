package fr.dev.sensei.guild.keeper.finance;

import java.util.Optional;

/** Port de persistance des comptes de guilde. */
public interface GuildAccountRepository {

    Optional<GuildAccount> findByGuildId(String guildId);

    void save(GuildAccount account);
}
