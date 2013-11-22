package cn.ac.ict.partition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

import cn.ac.ict.util.MutableInteger;

public class MySimpleLPPartitionVertex extends Vertex<LongWritable, LongWritable, IntWritable, LongWritable> {

	@Override
	public void compute(Iterable<LongWritable> messages) throws IOException {
		// TODO Auto-generated method stub
		long newLabel = getValue().get();
		//1: superstep0 initialize the value of vertex to its id
		if(getSuperstep() == 0){
			for(Edge<LongWritable, IntWritable> edge : getEdges()){
				long targetId = edge.getTargetVertexId().get();
				if(targetId<0){
					throw new IOException("the targetId should not be negative");
				}
				if(targetId<newLabel){
					newLabel = targetId;
				}
			}
		}
		//2: choose the label
		Map<Long, MutableInteger> labels = new HashMap<Long, MutableInteger>();
		for(LongWritable msg : messages){
			long ngrNewLabel = msg.get();
			MutableInteger initValue = new MutableInteger(1);
			MutableInteger oldValue = labels.put(ngrNewLabel, initValue);
			if(oldValue != null){
				initValue.set(oldValue.get()+1);
			}
		}
		List<Entry<Long, MutableInteger>> list = new ArrayList<Entry<Long, MutableInteger>>(labels.entrySet());
		Collections.sort(list, new Comparator<Entry<Long, MutableInteger>>() {

					@Override
					public int compare(Entry<Long, MutableInteger> o1, Entry<Long, MutableInteger> o2) {
						// TODO Auto-generated method stub
						return -o1.getValue().get().compareTo(o2.getValue().get());
					}
		});
		newLabel = list.get(0).getValue().get();

		//3: if the label is changed, sending massage to its neighbors.
		if(newLabel != getValue().get()){
			setValue(new LongWritable(newLabel));
			sendMessageToAllEdges(getValue());
		}
		//4: stop further computation
		voteToHalt();
		
	}

}
