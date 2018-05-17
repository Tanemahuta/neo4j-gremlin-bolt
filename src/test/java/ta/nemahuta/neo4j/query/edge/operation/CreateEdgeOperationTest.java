package ta.nemahuta.neo4j.query.edge.operation;

import com.google.common.collect.ImmutableMap;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.jupiter.api.Test;
import ta.nemahuta.neo4j.id.Neo4JElementId;
import ta.nemahuta.neo4j.id.Neo4JNativeElementIdAdapter;
import ta.nemahuta.neo4j.id.Neo4JPersistentElementId;
import ta.nemahuta.neo4j.id.Neo4JTransientElementId;
import ta.nemahuta.neo4j.query.AbstractStatementBuilderTest;

import javax.annotation.Nonnull;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ta.nemahuta.neo4j.testutils.MockUtils.mockProperties;

class CreateEdgeOperationTest extends AbstractStatementBuilderTest {

    @Nonnull
    private CreateEdgeOperation createOperation(final Neo4JElementId<?> id) {
        return new CreateEdgeOperation("n", "r", "m",
                id, "test", Direction.IN, mockProperties(ImmutableMap.of("golden", "retriever")),
                "props", new Neo4JNativeElementIdAdapter());
    }

    @Test
    void isNeedsStatement() {
        assertTrue(createOperation(new Neo4JTransientElementId<>(1l)).isNeedsStatement());
    }

    @Test
    void append() {
        assertBuildsStatement("CREATE (n)<-[r:`test`={props}]-(m) RETURN ID(r)",
                ImmutableMap.of("props", ImmutableMap.of("golden", "retriever")),
                createOperation(new Neo4JTransientElementId<>(1l))
        );
        assertBuildsStatement("CREATE (n)<-[r:`test`={props}]-(m)",
                ImmutableMap.of("props", ImmutableMap.of("golden", "retriever")),
                createOperation(new Neo4JPersistentElementId<>(1l)));
    }
}