package ta.nemahuta.neo4j.session.scope;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ta.nemahuta.neo4j.session.cache.HierarchicalCache;
import ta.nemahuta.neo4j.session.sync.Neo4JElementStateHandler;
import ta.nemahuta.neo4j.state.Neo4JElementState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class DefaultNeo4JElementStateScope<S extends Neo4JElementState> implements Neo4JElementStateScope<S> {

    /**
     * the cache to be used to getAll loaded elements
     */
    @NonNull
    private final HierarchicalCache<Long, S> hierarchicalCache;

    @NonNull
    private final Neo4JElementStateHandler remoteElementHandler;

    private final Set<Long> deleted = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void modify(final long id,
                       @Nonnull @NonNull final Function<S, S> modifier) {
        final S state = getAll(Collections.singleton(id)).get(id);
        if (state == null) {
            throw new IllegalStateException("Could not find entity in scope or remotely: " + id);
        }
        final S newState = Objects.requireNonNull(modifier.apply(state), "modified state cannot be null");
        if (!Objects.equals(newState, state)) {
            log.debug("Change detected for element with id: {}", id);
            hierarchicalCache.put(id, newState);
            remoteElementHandler.update(id, state, newState);
        }
    }

    @Override
    public void delete(final long id) {
        log.debug("Removing element with id from the queue: {}", id);
        deleted.add(id);
        hierarchicalCache.remove(id);
        remoteElementHandler.delete(id);
    }

    @Override
    @Nonnull
    public long create(@Nonnull @NonNull final S state) {
        final Long result = remoteElementHandler.create(state);
        hierarchicalCache.put(result, state);
        return result;
    }

    @Nullable
    @Override
    public S get(final long id) {
        return getAll(Collections.singleton(id)).get(id);
    }

    @Nullable
    @Override
    public Map<Long, S> getAll(@Nonnull @NonNull final Collection<Long> ids) {
        final Map<Long, S> results = new ConcurrentHashMap<>();
        final Set<Long> idsToBeLoaded = ids
                .parallelStream()
                .filter(id -> !deleted.contains(id)) // Deleted elements in the current scope will not be handled
                .filter(id ->
                        Optional.ofNullable(hierarchicalCache.get(id))
                                .map(state -> {
                                    // Put to map and remove from the ones to be loaded
                                    results.put(id, state);
                                    return false;
                                }).orElse(true)
                ).collect(Collectors.toSet());

        log.debug("Found the following ids to be in scope already: {}", results.keySet());
        log.debug("Loading the following ids from the remote: {}", idsToBeLoaded);

        // Now we load all the remaining elements and put them to the cache and in the results
        final Map<Long, S> loaded = remoteElementHandler.getAll(idsToBeLoaded);
        results.putAll(loaded);
        hierarchicalCache.putAll(loaded);

        return results;
    }

}
