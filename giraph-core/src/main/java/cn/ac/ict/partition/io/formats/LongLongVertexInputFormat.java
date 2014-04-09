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
package cn.ac.ict.partition.io.formats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.json.JSONArray;
import org.json.JSONException;

import cn.ac.ict.partition.VertexValue;
import cn.ac.ict.partition.io.formats.TextVertexFromMultiLinesInputFormat.TextVertexReaderFromMultiLinesProcessed;
import cn.ac.ict.partition.io.formats.TextVertexFromMultiLinesInputFormat.TextVertexReaderFromMultiLinesProcessedHandingExceptions;

import com.google.common.collect.Lists;

/**
  * VertexInputFormat that features <code>long</code> vertex ID's,
  * <code>double</code> vertex values and <code>float</code>
  * out-edge weights, and <code>double</code> message types,
  *  specified in JSON format.
  */
public class LongLongVertexInputFormat extends
  TextVertexFromMultiLinesInputFormat<LongWritable, VertexValue, IntWritable> {

  @Override
  public TextVertexReader createVertexReader(InputSplit split,
      TaskAttemptContext context) {
    return new LongLongVertexReader();
  }

 /**
  * VertexReader that features <code>double</code> vertex
  * values and <code>float</code> out-edge weights. The
  * files should be in the following JSON format:
  * JSONArray(<vertex id>, <vertex value>,
  *   JSONArray(JSONArray(<dest vertex id>, <edge value>), ...))
  * Here is an example with vertex id 1, vertex value 4.3, and two edges.
  * First edge has a destination vertex 2, edge value 2.1.
  * Second edge has a destination vertex 3, edge value 0.7.
  * [1,4.3,[[2,2.1],[3,0.7]]]
  */
  class LongLongVertexReader 
  	extends TextVertexReaderFromMultiLinesProcessedHandingExceptions<String[], Exception>{

	@Override
	protected String[] preprocessLine(Text line) throws Exception {
		String[] lines = line.toString().split(" ");
		if(lines.length != 2){
			throw new Exception("One line should contain 2 parameters.");
		}
		return lines;
	}

	@Override
	protected LongWritable getId(String[] line) {
		return new LongWritable(Long.valueOf(line[0]));
	}

	@Override
	protected VertexValue getValue(String[] line) {
		return new VertexValue(Long.valueOf(line[0]), 0);
	}

	@Override
	protected Iterable<Edge<LongWritable, IntWritable>> getEdges(String[] line) {
		List<Edge<LongWritable, IntWritable>> edges = new ArrayList<Edge<LongWritable,IntWritable>>();
		edges.add(EdgeFactory.create(new LongWritable(Long.valueOf(line[1])), new IntWritable(0)));
		return edges;
	}

   @Override
   protected Vertex<LongWritable, VertexValue, IntWritable,
   LongWritable> handleException(Text line, String[] lines,
       Exception e) {
	   throw new IllegalArgumentException(
			   "Couldn't get vertex from line " + line, e);
   }


  }
}
