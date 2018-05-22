package ta.nemahuta.neo4j.structure;

import lombok.NonNull;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import ta.nemahuta.neo4j.session.scope.Neo4JElementStateScope;
import ta.nemahuta.neo4j.state.Neo4JVertexState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Neo4J implementation of a {@link Vertex} for gremlin.
 *
 * @author Christian Heike (christian.heike@icloud.com)
 */
public class Neo4JVertex extends Neo4JElement<Neo4JVertexState, VertexProperty> implements Vertex {

    public static final String LABEL_DELIMITER = "::";

    private final EdgeProvider inEdgeProvider, outEdgeProvider;

    public Neo4JVertex(@Nonnull final Neo4JGraph graph, final long id,
                       @Nonnull final Neo4JElementStateScope<Neo4JVertexState> scope,
                       @Nonnull @NonNull final EdgeProvider inEdgeProvider,
                       @Nonnull @NonNull final EdgeProvider outEdgeProvider) {
        super(graph, id, scope);
        this.inEdgeProvider = inEdgeProvider;
        this.outEdgeProvider = outEdgeProvider;
    }


    @Override
    public Edge addEdge(final String label, final Vertex inVertex, final Object... keyValues) {
        if (!(inVertex instanceof Neo4JVertex)) {
            throw new IllegalArgumentException("Cannot connect a " + getClass().getSimpleName() + " to a " +
                    Optional.ofNullable(inVertex).map(Object::getClass).map(Class::getSimpleName).orElse(null));
        }
        final Neo4JEdge result = graph.addEdge(label, this, (Neo4JVertex) inVertex, keyValues);
        outEdgeProvider.register(result.label(), result.id());
        return result;
    }

    @Override
    public Iterator<Edge> edges(final Direction direction, final String... edgeLabels) {
        return graph.edges(edgeIdStream(direction, edgeLabels).collect(Collectors.toSet()).toArray());
    }

    @Override
    public Iterator<Vertex> vertices(@Nonnull @NonNull final Direction direction,
                                     @Nonnull @NonNull final String... edgeLabels) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(edges(direction, edgeLabels), Spliterator.ORDERED), false)
                .map(e -> e.inVertex().id() == this.id() ? e.inVertex() : e.outVertex())
                .iterator();
    }

    private Stream<Long> edgeIdStream(final Direction direction, final String... edgeLabels) {
        switch (Optional.ofNullable(direction).orElse(Direction.BOTH)) {
            case IN:
                return inEdgeIdStream(edgeLabels);
            case OUT:
                return outEdgeIdStream(edgeLabels);
            case BOTH:
                return Stream.concat(inEdgeIdStream(edgeLabels), outEdgeIdStream(edgeLabels));
            default:
                return throwDirectionNotHandled(direction);
        }
    }

    private Stream<Long> inEdgeIdStream(@Nonnull final String... labels) {
        return inEdgeProvider.provideEdges(labels).stream();
    }

    private Stream<Long> outEdgeIdStream(@Nonnull final String... labels) {
        return outEdgeProvider.provideEdges(labels).stream();
    }

    @Override
    public String label() {
        return getState().getLabels().stream().collect(Collectors.joining(LABEL_DELIMITER));
    }

    @Override
    public <V> Iterator<VertexProperty<V>> properties(final String... propertyKeys) {
        return getProperties(propertyKeys).map(p -> (VertexProperty<V>) p).iterator();
    }

    @Override
    public <V> VertexProperty<V> property(final VertexProperty.Cardinality cardinality,
                                          final String key,
                                          final V value,
                                          final java.lang.Object... keyValues) {
        final VertexProperty result = getProperty(key, value);
        ElementHelper.attachProperties(result, keyValues);
        return result;
    }

    @Nonnull
    private Stream<EdgeProvider> edgeProviderStream(final Direction direction) {
        switch (Optional.ofNullable(direction).orElse(Direction.BOTH)) {
            case IN:
                return Stream.of(inEdgeProvider);
            case OUT:
                return Stream.of(outEdgeProvider);
            case BOTH:
                return Stream.of(inEdgeProvider, outEdgeProvider);
            default:
                return throwDirectionNotHandled(direction);
        }
    }

    @Nonnull
    private Stream<Vertex> vertexOf(@Nonnull @NonNull final Direction direction,
                                    @Nonnull @NonNull final Edge edge) {
        switch (direction) {
            case IN:
                return Stream.of(edge.outVertex());
            case OUT:
                return Stream.of(edge.inVertex());
            case BOTH:
                return Stream.of(edge.inVertex(), edge.outVertex());
            default:
                return throwDirectionNotHandled(direction);
        }
    }

    private <T> T throwDirectionNotHandled(@Nullable @NonNull final Direction direction) {
        throw new IllegalStateException("Cannot handle direction: " + direction);
    }


    @Override
    protected VertexProperty createNewProperty(final String key) {
        return new Neo4JVertexProperty(this, key);
    }

    @Override
    protected VertexProperty createEmptyProperty() {
        return VertexProperty.empty();
    }
}
