/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.giraph.partition;

import org.apache.giraph.worker.WorkerInfo;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import java.util.Collection;

/**
 * Stores the {@link PartitionOwner} objects from the master and provides the
 * mapping of vertex to {@link PartitionOwner}. Also generates the partition
 * owner implementation.
 *
 * @param <I> Vertex id
 * @param <V> Vertex value
 * @param <E> Edge value
 * @param <M> Message data
 */
@SuppressWarnings("rawtypes")
public interface WorkerGraphPartitioner<I extends WritableComparable,
    V extends Writable, E extends Writable, M extends Writable> {
  /**
   * Instantiate the {@link PartitionOwner} implementation used to read the
   * master assignments.
   *
   * @return Instantiated {@link PartitionOwner} object
   */
  PartitionOwner createPartitionOwner();

  /**
   * Figure out the owner of a vertex
   *
   * @param vertexId Vertex id to get the partition for
   * @return Correct partition owner
   */
  PartitionOwner getPartitionOwner(I vertexId);

  /**
   * At the end of a superstep, workers have {@link PartitionStats} generated
   * for each of their partitions.  This method will allow the user to
   * modify or create their own {@link PartitionStats} interfaces to send to
   * the master.
   *
   * @param workerPartitionStats Stats generated by the infrastructure during
   *        the superstep
   * @param partitionStore Partition store for this worker
   *        (could be used to provide more useful stat information)
   * @return Final partition stats
   */
  Collection<PartitionStats> finalizePartitionStats(
      Collection<PartitionStats> workerPartitionStats,
      PartitionStore<I, V, E, M> partitionStore);

  /**
   * Get the partitions owners and update locally.  Returns the partitions
   * to send to other workers and other dependencies.
   *
   * @param myWorkerInfo Worker info.
   * @param masterSetPartitionOwners Master set partition owners, received
   *        prior to beginning the superstep
   * @param partitionStore Partition store for this worker
   *        (can be used to fill the return map of partitions to send)
   * @return Information for the partition exchange.
   */
  PartitionExchange updatePartitionOwners(
      WorkerInfo myWorkerInfo,
      Collection<? extends PartitionOwner> masterSetPartitionOwners,
      PartitionStore<I, V, E, M> partitionStore);

  /**
   * Get a collection of the {@link PartitionOwner} objects.
   *
   * @return Collection of owners for every partition.
   */
  Collection<? extends PartitionOwner> getPartitionOwners();
}
