package ta.nemahuta.neo4j.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.tinkerpop.gremlin.structure.Direction;

import javax.annotation.Nonnull;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Neo4JVertexState extends Neo4JElementState {

    @Getter
    protected final ImmutableSet<String> labels;

    @Getter
    protected final VertexEdgeReferences incomingEdgeIds, outgoingEdgeIds;


    public Neo4JVertexState(@Nonnull final ImmutableSet<String> labels,
                            @Nonnull final ImmutableMap<String, Object> properties) {
        this(labels, properties, new VertexEdgeReferences(), new VertexEdgeReferences());
    }

    public Neo4JVertexState(@Nonnull final ImmutableSet<String> labels,
                            @Nonnull final ImmutableMap<String, Object> properties,
                            @Nonnull final VertexEdgeReferences incomingEdgeIds,
                            @Nonnull final VertexEdgeReferences outgoingEdgeIds) {
        super(properties);
        this.labels = labels;
        this.incomingEdgeIds = incomingEdgeIds;
        this.outgoingEdgeIds = outgoingEdgeIds;
    }

    public Neo4JVertexState withRemovedEdges(final Set<Long> removedIds) {
        return withIncomingEdgeIds(this.incomingEdgeIds.withRemovedEdges(removedIds))
                .withOutgoingEdgeIds(this.outgoingEdgeIds.withRemovedEdges(removedIds));
    }

    public VertexEdgeReferences getEdgeIds(@Nonnull final Direction direction) {
        switch (direction) {
            case IN:
                return getIncomingEdgeIds();
            case OUT:
                return getOutgoingEdgeIds();
            default:
                throw new IllegalArgumentException("Cannot get references for both.");
        }
    }

    public Neo4JVertexState withEdgeIds(@Nonnull final Direction direction, @Nonnull final VertexEdgeReferences references) {
        switch (direction) {
            case IN:
                return withIncomingEdgeIds(references);
            case OUT:
                return withOutgoingEdgeIds(references);
            default:
                throw new IllegalArgumentException("Cannot change references for both.");
        }
    }

    @Override
    public Neo4JVertexState withProperties(@Nonnull final ImmutableMap<String, Object> properties) {
        if (this.properties == properties) {
            return this;
        }
        return new Neo4JVertexState(labels, properties, incomingEdgeIds, outgoingEdgeIds);
    }

    public Neo4JVertexState withIncomingEdgeIds(@Nonnull final VertexEdgeReferences incomingEdgeIds) {
        if (this.incomingEdgeIds == incomingEdgeIds) {
            return this;
        }
        return new Neo4JVertexState(labels, properties, incomingEdgeIds, outgoingEdgeIds);
    }

    public Neo4JVertexState withOutgoingEdgeIds(@Nonnull final VertexEdgeReferences outgoingEdgeIds) {
        if (this.outgoingEdgeIds == outgoingEdgeIds) {
            return this;
        }
        return new Neo4JVertexState(labels, properties, incomingEdgeIds, outgoingEdgeIds);
    }

}
