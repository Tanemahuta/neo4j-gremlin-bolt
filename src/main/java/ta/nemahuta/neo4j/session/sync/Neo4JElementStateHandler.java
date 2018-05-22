package ta.nemahuta.neo4j.session.sync;

import ta.nemahuta.neo4j.state.Neo4JElementState;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public interface Neo4JElementStateHandler<S extends Neo4JElementState> {

    /**
     * Loads the {@link Neo4JElementState} of the ids to be loaded.
     *
     * @param idsToBeLoaded the elements to be loaded
     * @return the {@link Map} of the elements found
     */
    @Nonnull
    Map<Long, S> getAll(@Nonnull Set<Long> idsToBeLoaded);

    /**
     * Update an element.
     *
     * @param id           the id of the element
     * @param currentState the current state
     * @param newState     the new state to update to
     */
    void update(long id, @Nonnull S currentState, @Nonnull S newState);

    /**
     * Deletes an element with the provided id.
     *
     * @param id the id of the element
     */
    void delete(long id);

    /**
     * Create a new element with the state.
     *
     * @param state the state of the element.
     * @return the persisted id
     */
    long create(@Nonnull S state);

}
