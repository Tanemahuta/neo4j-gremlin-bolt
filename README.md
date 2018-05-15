# neo4j-gremlin-bolt

This project allows the use of the [Apache Tinkerpop](http://tinkerpop.apache.org/) Java API with the [neo4j server](http://neo4j.com/) using the [BOLT](https://github.com/neo4j/neo4j-java-driver) protocol.

# Kudos

I forked this project from [Steelbridge Labs](https://github.com/SteelBridgeLabs/neo4j-gremlin-bolt) and alhough I have nearly re-engineered
each and every part of the code, I want to acknowledge their work. 

## Build status

![Build Status](https://travis-ci.org/Tanemahuta/neo4j-gremlin-bolt.svg?branch=develop)
![Coverage Status](https://codecov.io/gh/Tanemahuta/neo4j-gremlin-bolt/branch/develop/graph/badge.svg)

## Requirements for building

* Java 8.
* Gradle 4.6 or newer (use the wrapper, please)

## Usage

Add the Neo4j [Apache Tinkerpop](http://tinkerpop.apache.org/) implementation to your project:

### Maven

```xml
    <dependency>
        <groupId>ta.nemahuta.neo4j</groupId>
        <artifactId>neo4j-gremlin-bolt</artifactId>
        <version>{version}</version>
    </dependency>
```

### Gradle
```groovy
dependencies {
    compile 'ta.nemahuta.neo4j:neo4j-gremlin-bolt:{version}'
}
```

## License

neo4j-gremlin-bolt and it's modules are licensed under the [Apache License v 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Features

* [Apache Tinkerpop](http://tinkerpop.apache.org/) 3.x Online Transactional Processing Graph Systems (OLTP) support.
* [neo4j](http://neo4j.com/) implementation on top of the [BOLT](https://github.com/neo4j/neo4j-java-driver) protocol.

# Graph API

## Element ID providers
**TODO rewrite this section**
The library supports an open architecture for element ID generation for new Vertices and Edges. The following element ID providers are supported out of the box:

### Neo4J native id() support, see [Neo4JNativeElementIdProvider](https://github.com/SteelBridgeLabs/neo4j-gremlin-bolt/blob/master/src/main/java/com/steelbridgelabs/oss/neo4j/structure/providers/Neo4JNativeElementIdProvider.java) for more information.

```java
    // create id provider
    Neo4JElementIdProvider<?> provider = new Neo4JNativeElementIdProvider();
```
Pros:

 * IDs are stored as `java.lang.Long` instances.
 * Fewer database hits on MATCH statements since index lookups are not required at the time of locating an entity by id: `MATCH (n:Label) WHERE ID(n) = {id} RETURN n`

Cons:

 * CREATE statements will run slower since the entity id must be retrieved from the database after insertion: `CREATE (n:label{field1: value, ..., fieldN: valueN}) RETURN ID(n)` 
 * Entity IDs in Neo4J are not guaranteed to be the same after a database restart/upgrade. Storing links to Neo4J entities outside the database based on IDs could become invalid after a database restart/upgrade. 
 
### Database sequence support, see [DatabaseSequenceElementIdProvider](https://github.com/SteelBridgeLabs/neo4j-gremlin-bolt/blob/master/src/main/java/com/steelbridgelabs/oss/neo4j/structure/providers/DatabaseSequenceElementIdProvider.java) for more information.

```java
    // create id provider
    Neo4JElementIdProvider<?> provider = new DatabaseSequenceElementIdProvider(driver);
```
Pros:

 * IDs are stored as `java.lang.Long` instances.
 * CREATE statements will run faster since there is no need to retrieve the entity after an insert operation: `CREATE (n:label{id: 1, field1: value, ..., fieldN: valueN})` 
 * Entity IDs are guaranteed to be the same after a database restart/upgrade since they are stored as property values. 

Cons:

 * A unique index is required for each one of the Labels used in your model.
 * More database hits on MATCH statements since an index lookup is required in order to locate an entity by id: `MATCH (n:Label) WHERE n.id = {id} RETURN n`

### Custom providers, by implementing the [Neo4JElementIdProvider](https://github.com/SteelBridgeLabs/neo4j-gremlin-bolt/blob/master/src/main/java/com/steelbridgelabs/oss/neo4j/structure/Neo4JElementIdProvider.java) interface.

## Connecting to the database

* Create driver instance, see [neo4j-java-driver](https://github.com/neo4j/neo4j-java-driver) for more information.

```java
    // create driver instance
    Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "neo4j"));
```

* Create element id provider instances, see [providers](#element-id-providers) for more information. 

```java
    // create id provider instances
    vertexIdProvider = ...
    edgeIdProvider = ...
```

* Create [Graph](http://tinkerpop.apache.org/javadocs/current/core/org/apache/tinkerpop/gremlin/structure/Graph.html) instance.

```java
    // create graph instance
    try (Graph graph = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider)) {
        
    }
```

## Working with transactions

* Obtain a [Transaction](http://tinkerpop.apache.org/javadocs/current/core/org/apache/tinkerpop/gremlin/structure/Transaction.html) instance from current Graph.

```java
    // create graph instance
    try (Graph graph = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider)) {
        // begin transaction
        try (Transaction transaction = graph.tx()) {
            // use Graph API to create, update and delete Vertices and Edges
            
            // commit transaction
            transaction.commit();
        }
    }
```

## Enabling Neo4J profiler

* Set logger INFO level to the package: com.steelbridgelabs.oss.neo4j.structure.summary 

* Enable profiler to the [Graph](http://tinkerpop.apache.org/javadocs/current/core/org/apache/tinkerpop/gremlin/structure/Graph.html) instance.

```java
    // create graph instance
    try (Neo4JGraph graph = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider)) {
        // enable profiler
        graph.setProfilerEnabled(true);
        
    }
```

The library will prefix CYPHER statements with the PROFILE clause dumping the output into the log file, example: 

````
2016-08-26 23:19:42.226  INFO 98760 --- [-f6753a03391b-1] c.s.o.n.s.summary.ResultSummaryLogger    : Profile for CYPHER statement: Statement{text='PROFILE MATCH (n:Person{id: {id}})-[r:HAS_ADDRESS]->(m) RETURN n, r, m', parameters={id: 1306984}}

+----------------------+----------------+------+---------+-----------+
| Operator             + Estimated Rows + Rows + DB Hits + Variables |
+----------------------+----------------+------+---------+-----------+
| +ProduceResults      |              0 |    1 |       0 | m, n, r   |
| |                    +----------------+------+---------+-----------+
| +Expand(All)         |              0 |    1 |       2 | m, n, r   |
| |                    +----------------+------+---------+-----------+
| +Filter              |              0 |    1 |       1 | n         |
| |                    +----------------+------+---------+-----------+
| +NodeUniqueIndexSeek |              0 |    1 |       2 | n         |
+----------------------+----------------+------+---------+-----------+
````

## Working with Vertices and Edges

### Create a Vertex

Create a new [Vertex](http://tinkerpop.apache.org/javadocs/current/core/org/apache/tinkerpop/gremlin/structure/Vertex.html) in the current `graph` call the [Graph.addVertex()](http://tinkerpop.apache.org/javadocs/current/core/org/apache/tinkerpop/gremlin/structure/Graph.html#addVertex-java.lang.Object...-) method.

```java
  // create a vertex in current graph
  Vertex vertex = graph.addVertex();
```

Create a new [Vertex](http://tinkerpop.apache.org/javadocs/current/core/org/apache/tinkerpop/gremlin/structure/Vertex.html) in the current `graph` with property values: 

```java
  // create a vertex in current graph with property values
  Vertex vertex = graph.addVertex("name", "John", "age", 50);
```

Create a new [Vertex](http://tinkerpop.apache.org/javadocs/current/core/org/apache/tinkerpop/gremlin/structure/Vertex.html) in the current `graph` with a Label: 

```java
  // create a vertex in current graph with label
  Vertex vertex1 = graph.addVertex("Person");
  // create another vertex in current graph with label
  Vertex vertex2 = graph.addVertex(T.label, "Company");
```

## Building the library

To compile the code and run all the unit tests:

````
mvn clean install
````

To run the Tinkerpop integration tests you need a running instance of the neo4j
server. The easiest way to get one up and running is by using the official neo4j
docker image:

````
docker run -d --name neo4j -p 7687:7687 -e NEO4J_AUTH=neo4j/neo4j123 neo4j:3.2-enterprise
````

And then execute the integration tests by running the following command:

````
mvn test -Pintegration-test
````
