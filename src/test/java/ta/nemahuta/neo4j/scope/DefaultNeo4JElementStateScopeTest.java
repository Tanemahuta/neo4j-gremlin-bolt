package ta.nemahuta.neo4j.scope;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ta.nemahuta.neo4j.cache.HierarchicalCache;
import ta.nemahuta.neo4j.handler.Neo4JElementStateHandler;
import ta.nemahuta.neo4j.state.Neo4JElementState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultNeo4JElementStateScopeTest {

    @Mock
    private HierarchicalCache<Long, Neo4JElementState> cache;

    @Mock
    private Neo4JElementStateHandler<Neo4JElementState> handler;

    @Mock
    private Neo4JElementState state, modifiedState;

    private Neo4JElementStateScope<Neo4JElementState> sut;

    @BeforeEach
    void createSut() {
        this.sut = new DefaultNeo4JElementStateScope<>(cache, handler);
        when(cache.get(1l)).thenReturn(state);
    }

    @Test
    void update() {
        // when: 'updating the state'
        sut.update(1l, modifiedState);
        // then: 'the cache is notified and the handler as well'
        verify(cache, times(1)).put(1l, modifiedState);
        verify(handler, times(1)).update(1l, state, modifiedState);
    }

    @Test
    void delete() {
        // when: 'deleting the state'
        sut.delete(1l);
        // then: 'the cache is notified and the handler as well'
        verify(cache, times(1)).remove(1l);
        verify(handler, times(1)).delete(1l);
    }

    @Test
    void create() {
        when(handler.create(modifiedState)).thenReturn(2l);
        // when: 'creating a element'
        sut.create(modifiedState);
        // then: 'the state is put to the cache by the id from the handler'
        verify(cache, times(1)).put(2l, modifiedState);
    }

    @Test
    void get() {
        // expect : 'requesting the state for a known item returns the item'
        assertEquals(state, sut.get(1l));
        assertNull(sut.get(2l));
    }

    @Test
    void getAll() {
        // when: 'loading unknown items'
        assertEquals(ImmutableMap.of(1l, state), sut.getAll(ImmutableSet.of(1l, 2l, 3l, 4l)));
        // then: 'the rest should have been loaded'
        verify(handler, times(1)).getAll(ImmutableSet.of(2l, 3l, 4l));
    }

}