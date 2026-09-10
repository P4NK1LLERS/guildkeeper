package fr.dev.sensei.guild.keeper.missions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementation in-memory du {@link QuestRepository}. */
public class InMemoryQuestRepository implements QuestRepository {

    private final Map<String, Quest> questsById = new LinkedHashMap<>();

    @Override
    public Optional<Quest> findById(String id) {
        return Optional.ofNullable(questsById.get(id));
    }

    @Override
    public List<Quest> findAll() {
        return new ArrayList<>(questsById.values());
    }

    @Override
    public void save(Quest quest) {
        questsById.put(quest.id(), quest);
    }
}
