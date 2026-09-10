package fr.dev.sensei.guild.keeper.missions;

import java.util.List;
import java.util.Optional;

/** Port de persistance des quetes du catalogue. */
public interface QuestRepository {

    Optional<Quest> findById(String id);

    List<Quest> findAll();

    void save(Quest quest);
}
