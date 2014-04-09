package cn.ac.ict.partition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.giraph.conf.GiraphConstants;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

import cn.ac.ict.util.MutableDouble;

public class MyLPAWithHubVertex extends Vertex<LongWritable, LongWritable, IntWritable, MessageValueForHop> {

	private static Logger LOG = Logger.getLogger("tracerLogger");
	private static Logger DEBUGLOG = Logger.getLogger("debugLogger");
	
	private double score = 0.0;
	
	@Override
	public void compute(Iterable<MessageValueForHop> messages) throws IOException {
		// TODO Auto-generated method stub
		long newLabel = getValue().get();
		long currentId = getId().get();
		printMsgs(messages);
		//1: superstep0 initialize the value of vertex to its id
		if(getSuperstep()==0){
			if(getId().get() == 20){
				for(Edge<LongWritable, IntWritable> e : getEdges()){
					DEBUGLOG.info("eid: "+getId().get()+" edges: "+e.getTargetVertexId().get());
				}
				DEBUGLOG.info("enid: "+getId().get()+" edges number: "+ getNumEdges());
			}
			LOG.info(String.format("%1$02d", currentId));
			return;
		}else if(getSuperstep() == 1){
			if(getId().get() == 20){
				for(Edge<LongWritable, IntWritable> e : getEdges()){
					DEBUGLOG.info("eid1: "+getId().get()+" edges: "+e.getTargetVertexId().get());
				}
				DEBUGLOG.info("enid1: "+getId().get()+" edges number: "+ getNumEdges());
			}
			newLabel = currentId;
			score = 1.0;
		}else{
			//2: choose the label by neighbors' score
			Map<Long, MutableDouble> labelToScore = new HashMap<Long, MutableDouble>();
			//count the message for selection.
			for(MessageValueForHop msg : messages){
				long ngrNewLabel = msg.getSrcNewLabel();
				double ngrWeight = msg.getSrcScore()*msg.getNumEdges();
				DEBUGLOG.info("***Score: "+ngrWeight);
				if(ngrWeight<=0.0){
					continue;
				}
				MutableDouble initScore = new MutableDouble(ngrWeight);
				MutableDouble oldScore = labelToScore.put(ngrNewLabel, initScore);
				if(oldScore != null){
					initScore.set(oldScore.get()+ngrWeight);
				}
			}
			List<Entry<Long, MutableDouble>> list = new ArrayList<Entry<Long, MutableDouble>>(labelToScore.entrySet());
			//here, because discarded the ngrWeight which is less than zero, so the list might be empty.
			if(!list.isEmpty()){
				Collections.sort(list, new Comparator<Entry<Long, MutableDouble>>() {

					@Override
					public int compare(Entry<Long, MutableDouble> o1, Entry<Long, MutableDouble> o2) {
						return o2.getValue().get().compareTo(o1.getValue().get());
					}
				});
				double maxScore = list.get(0).getValue().get();	
				DEBUGLOG.info("***maxScore: "+maxScore);
				//is number of new label more than old label
				//number of old label equals to #edges - #messages
				long minLabel = Long.MAX_VALUE;
				for(Entry<Long, MutableDouble> entry : list){
					//just compare all label with max score
					if(maxScore == entry.getValue().get()){
						//if current label is max, so don't exchange
						if(entry.getKey() == getValue().get()){
							minLabel = entry.getKey();
							break;
						}
						//get the min Label
						if(minLabel > entry.getKey()){
							minLabel = entry.getKey();
						}
					}else{
						break;
					}
				}
				newLabel = minLabel;
				for(MessageValueForHop msg : messages){
					if(newLabel == msg.getSrcNewLabel()){
						if(score < msg.getSrcScore()){
							score = msg.getSrcScore();
						}
					}
				}
				DEBUGLOG.info("***finalScore: "+score+" newLabel: "+newLabel);
				score -= GiraphConstants.HOP_ATTENUATION.get(getConf());
				DEBUGLOG.info("***finalScore: "+score+" newLabel: "+newLabel);
			}
		}
		LOG.info(String.format("%1$02d", newLabel));
		//3: if the label is changed, sending massage to its neighbors.
		if(newLabel != getValue().get()){
			setValue(new LongWritable(newLabel));
			MessageValueForHop mvh = new MessageValueForHop(getId().get(), getValue().get(), score, getNumEdges());
			sendMessageToAllEdges(mvh);
			//send score and number of edges to itself to make sure message score is larger than itself
			sendMessage(getId(), mvh);
			DEBUGLOG.info("smid:"+getSuperstep()+":"+getId().get()+" send messages: "+(getNumEdges()+1));
		}else{
			DEBUGLOG.info("smid:"+getSuperstep()+":"+getId().get()+" send messages: 0");
		}
		DEBUGLOG.info("###################");
		//4: stop further computation
		voteToHalt();
		
	}
	
	private void printEdges() throws IOException{
		for(Edge<LongWritable, IntWritable> edge : getEdges()){
			long targetId = edge.getTargetVertexId().get();
			if(targetId<0){
				throw new IOException("the targetId should not be negative");
			}
			DEBUGLOG.info("id:"+getSuperstep()+":"+getId().get()+"  edges: "+edge.getTargetVertexId().get()+", ");
		}
	}
	
	private void printMsgs(Iterable<MessageValueForHop> messages){
		int numMessages = 0;
		for(MessageValueForHop msg : messages){
			long id = msg.getSrcId();
			long ngrNewLabel = msg.getSrcNewLabel();
			double ngrScore = msg.getSrcScore();
			long ngrNumEdges = msg.getNumEdges();
			DEBUGLOG.info("id: "+getSuperstep()+":"+getId().get()+":"+score
					+"  edges: "+id+":"+ngrNewLabel+":"+ngrScore+":"+ngrNumEdges+", ");
			numMessages++;
		}
		if(numMessages != 0){
			DEBUGLOG.info("rmid:"+getSuperstep()+":"+getId().get()+" recieve messages: "+numMessages);
		}
	}

}
