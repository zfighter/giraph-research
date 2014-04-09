package cn.ac.ict.partition;

import java.io.File;
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
import org.apache.log4j.Logger;

import cn.ac.ict.util.MutableInteger;

public class MySimpleLPAVertex extends Vertex<LongWritable, LongWritable, IntWritable, MessageValue> {

	private static Logger LOG = Logger.getLogger("tracerLogger");
	private static Logger DEBUGLOG = Logger.getLogger("debugLogger");
	
	@Override
	public void compute(Iterable<MessageValue> messages) throws IOException {
		// TODO Auto-generated method stub
		long newLabel = getValue().get();
		long currentId = getId().get();
		printMsgs(messages);
		//1: superstep0 initialize the value of vertex to its id
		if(getSuperstep()==0){
			LOG.info(String.format("%1$02d", currentId));
			return;
		}else if(getSuperstep() == 1){
			newLabel = currentId;
			for(Edge<LongWritable, IntWritable> edge : getEdges()){
				long targetId = edge.getTargetVertexId().get();
				if(targetId<0){
					throw new IOException("the targetId should not be negative");
				}
				if(targetId<newLabel){
					newLabel = targetId;
				}
			}
		}else{
			//2: choose the label
			Map<Long, MutableInteger> labels = new HashMap<Long, MutableInteger>();
			//count the message for selection.
			long NumMsgs = 0;
			for(MessageValue msg : messages){
				long ngrNewLabel = msg.getSrcNewLabel();
				MutableInteger initValue = new MutableInteger(1);
				MutableInteger oldValue = labels.put(ngrNewLabel, initValue);
				if(oldValue != null){
					initValue.set(oldValue.get()+1);
				}
				NumMsgs++;
			}
			List<Entry<Long, MutableInteger>> list = new ArrayList<Entry<Long, MutableInteger>>(labels.entrySet());
			Collections.sort(list, new Comparator<Entry<Long, MutableInteger>>() {

						@Override
						public int compare(Entry<Long, MutableInteger> o1, Entry<Long, MutableInteger> o2) {
							return o2.getValue().get().compareTo(o1.getValue().get());
						}
			});
			int maxNum = list.get(0).getValue().get();
			//is number of new label more than old label
			//number of old label equals to #edges - #messages
			if(maxNum > getNumEdges()-NumMsgs){
				long minLabel = Long.MAX_VALUE;
				for(Entry<Long, MutableInteger> entry : list){
					if(maxNum == entry.getValue().get()){
						if(minLabel > entry.getKey()){
							minLabel = entry.getKey();
						}
					}else{
						break;
					}
				}
				newLabel = minLabel;
			}
		}
		LOG.info(String.format("%1$02d", newLabel));
		//3: if the label is changed, sending massage to its neighbors.
		if(newLabel != getValue().get()){
			setValue(new LongWritable(newLabel));
			sendMessageToAllEdges(new MessageValue(getId().get(), getValue().get()));
		}
		//4: stop further computation
		voteToHalt();
		
	}
	
	private void printEdges() throws IOException{
		for(Edge<LongWritable, IntWritable> edge : getEdges()){
			long targetId = edge.getTargetVertexId().get();
			if(targetId<0){
				throw new IOException("the targetId should not be negative");
			}
			DEBUGLOG.info("id: "+getSuperstep()+":"+getId().get()+"  edges,: "+edge.getTargetVertexId().get()+", ");
		}
	}
	
	private void printMsgs(Iterable<MessageValue> messages){
		for(MessageValue msg : messages){
			long id = msg.getSrcId();
			long ngrNewLabel = msg.getSrcNewLabel();
			DEBUGLOG.info("id: "+getSuperstep()+":"+getId().get()+"  edges,: "+id+":"+ngrNewLabel+", ");
		}
	}

}
