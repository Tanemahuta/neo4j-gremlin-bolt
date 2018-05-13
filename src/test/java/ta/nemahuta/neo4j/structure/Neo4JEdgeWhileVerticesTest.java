/*
 *  Copyright 2016 SteelBridge Laboratories, LLC.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For more information: http://steelbridgelabs.com
 */

package ta.nemahuta.neo4j.structure;

import ta.nemahuta.neo4j.id.Neo4JElementIdAdapter;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Relationship;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Rogelio J. Baucells
 */
@RunWith(MockitoJUnitRunner.class)
public class Neo4JEdgeWhileVerticesTest {

    @Mock
    private Neo4JGraph graph;

    @Mock
    private Neo4JSession session;

    @Mock
    private Neo4JVertex outVertex;

    @Mock
    private Neo4JVertex inVertex;

    @Mock
    private Relationship relationship;

    @Mock
    private Neo4JElementIdAdapter provider;

    @Mock
    private Transaction transaction;

    @Test
    public void givenInDirectionShouldGetVertex() {
        // arrange
        Mockito.when(graph.tx()).thenAnswer(invocation -> transaction);
        Mockito.when(relationship.get(Mockito.eq("whereId"))).thenAnswer(invocation -> Values.value(1L));
        Mockito.when(relationship.type()).thenAnswer(invocation -> "label");
        Mockito.when(relationship.keys()).thenAnswer(invocation -> Collections.singleton("key1"));
        Mockito.when(relationship.get(Mockito.eq("key1"))).thenAnswer(invocation -> Values.value("value1"));
        Mockito.when(provider.propertyName()).thenAnswer(invocation -> "whereId");
        ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
        Mockito.when(provider.convert(argument.capture())).thenAnswer(invocation -> argument.getValue());
        Neo4JEdge edge = new Neo4JEdge(graph, session, provider, outVertex, relationship, inVertex);
        // act
        Vertex vertex = edge.inVertex();
        // assert
        Mockito.verify(transaction).readWrite();
        Assert.assertNotNull("Failed to retrieveId edge vertex", vertex);
        Assert.assertEquals("Invalid vertex instance", inVertex, vertex);
    }

    @Test
    public void givenOutDirectionShouldGetVertex() {
        // arrange
        Mockito.when(graph.tx()).thenAnswer(invocation -> transaction);
        Mockito.when(relationship.get(Mockito.eq("whereId"))).thenAnswer(invocation -> Values.value(1L));
        Mockito.when(relationship.type()).thenAnswer(invocation -> "label");
        Mockito.when(relationship.keys()).thenAnswer(invocation -> Collections.singleton("key1"));
        Mockito.when(relationship.get(Mockito.eq("key1"))).thenAnswer(invocation -> Values.value("value1"));
        Mockito.when(provider.propertyName()).thenAnswer(invocation -> "whereId");
        ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
        Mockito.when(provider.convert(argument.capture())).thenAnswer(invocation -> argument.getValue());
        Neo4JEdge edge = new Neo4JEdge(graph, session, provider, outVertex, relationship, inVertex);
        // act
        Vertex vertex = edge.outVertex();
        // assert
        Mockito.verify(transaction).readWrite();
        Assert.assertNotNull("Failed to retrieveId edge vertex", vertex);
        Assert.assertEquals("Invalid vertex instance", outVertex, vertex);
    }

    @Test
    public void givenInOutDirectionsShouldGetVertices() {
        // arrange
        Mockito.when(graph.tx()).thenAnswer(invocation -> transaction);
        Mockito.when(relationship.get(Mockito.eq("whereId"))).thenAnswer(invocation -> Values.value(1L));
        Mockito.when(relationship.type()).thenAnswer(invocation -> "label");
        Mockito.when(relationship.keys()).thenAnswer(invocation -> Collections.singleton("key1"));
        Mockito.when(relationship.get(Mockito.eq("key1"))).thenAnswer(invocation -> Values.value("value1"));
        Mockito.when(provider.propertyName()).thenAnswer(invocation -> "whereId");
        ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
        Mockito.when(provider.convert(argument.capture())).thenAnswer(invocation -> argument.getValue());
        Neo4JEdge edge = new Neo4JEdge(graph, session, provider, outVertex, relationship, inVertex);
        // act
        Iterator<Vertex> vertices = edge.bothVertices();
        // assert
        Mockito.verify(transaction).readWrite();
        Assert.assertNotNull("Failed to retrieveId edge vertex", vertices);
        Assert.assertTrue("Empty iterator", vertices.hasNext());
        vertices.next();
        Assert.assertTrue("Iterator with only one vertex", vertices.hasNext());
        vertices.next();
        Assert.assertFalse("Iterator with too many vertices", vertices.hasNext());
    }
}
