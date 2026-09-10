package fr.dev.sensei.guild.keeper.recruitment;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance des membres. Interface volontairement minimale : elle sert
 * surtout de point d'isolation pour Mockito dans les tests.
 */
public interface MemberRepository {

    Optional<Member> findById(String id);

    Optional<Member> findByName(String name);

    List<Member> findAll();

    void save(Member member);
}
