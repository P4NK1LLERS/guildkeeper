package fr.dev.sensei.guild.keeper.finance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Implementation in-memory du {@link GuildAccountRepository}. */
public class InMemoryGuildAccountRepository implements GuildAccountRepository {

    private final Map<String, GuildAccount> accountsByGuildId = new LinkedHashMap<>();

    @Override
    public Optional<GuildAccount> findByGuildId(String guildId) {
        return Optional.ofNullable(accountsByGuildId.get(guildId));
    }

    @Override
    public void save(GuildAccount account) {
        accountsByGuildId.put(account.guildId(), account);
    }
}
