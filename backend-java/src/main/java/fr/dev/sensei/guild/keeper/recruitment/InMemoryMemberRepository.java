package fr.dev.sensei.guild.keeper.recruitment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation in-memory du {@link MemberRepository}. Suffisante pour le cours :
 * pas de base de donnees, l'isolation se fait par Mockito la ou c'est utile.
 */
public class InMemoryMemberRepository implements MemberRepository {

    private final Map<String, Member> membersById = new LinkedHashMap<>();

    @Override
    public Optional<Member> findById(String id) {
        return Optional.ofNullable(membersById.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        return membersById.values().stream()
                .filter(member -> member.name().equals(name))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(membersById.values());
    }

    @Override
    public void save(Member member) {
        membersById.put(member.id(), member);
    }
}
