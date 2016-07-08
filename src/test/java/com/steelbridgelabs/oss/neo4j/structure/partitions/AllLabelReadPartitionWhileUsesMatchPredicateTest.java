/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.steelbridgelabs.oss.neo4j.structure.partitions;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JReadPartition;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Rogelio J. Baucells
 */
public class AllLabelReadPartitionWhileUsesMatchPredicateTest {

    @Test
    public void givenPartitionShouldReturnFalse() {
        // arrange
        Neo4JReadPartition partition = new AllLabelReadPartition("l1", "l2", "l3");
        // act
        boolean result = partition.usesMatchPredicate();
        // assert
        Assert.assertFalse("Invalid response for AllLabelReadPartition", result);
    }
}