package cn.ac.ict.partition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

import cn.ac.ict.util.MutableInteger;

public class DistributeEdges extends Vertex<LongWritable, VertexValue, IntWritable, MessageValueForHop> {
	
	private static Logger LOG = Logger.getLogger(DistributeEdges.class);
	private static Logger TRACE = LOG.getLogger("tracerLogger");

	@Override
	public void compute(Iterable<MessageValueForHop> messages) throws IOException {
		int[] src = hashToServers(getId().get(), 5);
		TRACE.info(getId().get()+": ");
		printArray(src);
		TRACE.info(" : edges="+getNumEdges()+"\n");
		Map<Integer, MutableInteger> counts = new HashMap<Integer, MutableInteger>();
		for(int i = 0; i < src.length; i++){
			counts.put(src[i], new MutableInteger(0));
		}
		int[] trg = null;
		for(Edge<LongWritable, IntWritable> e : getEdges()) {
			trg = hashToServers(e.getTargetVertexId().get(), 5);
			for(int j = 0; j < trg.length; j++){
				MutableInteger oldvalue = counts.get(trg[j]);
				if(oldvalue != null){
					oldvalue.set(oldvalue.get()+1);
				}
			}
			TRACE.info(e.getTargetVertexId().get()+": ");
			printArray(trg);
			TRACE.info(" ; \n");
			trg = null;
		}
		for(Integer key : counts.keySet()) {
			TRACE.info(key+":"+counts.get(key).get());
		}
		TRACE.info("\n");
	}
	
	private int[] hashToServers(long vertex, int number) throws IOException{
		if(number == 0){
			throw new IOException("the value of NUMBER should not be 0.");
		}
		long base = getConf().getLong("base.hash", 5);
		int[] servers = new int[number];
		int serversNum = getConf().getInt("number.servers", 4);
		for(int i = 0; i < number; i++){
			long hashcode = hash(vertex, base);
			servers[i] = (int)(hashcode % serversNum);
			base += 3;
		}
		return servers;
	}
	
	private long hash(long vertexId, long base){
		if(base == 0)
			base = 5381l;
		long hash = base;
		hash = ((hash << 5) + hash) + vertexId;
		return hash;
	}
	
	private void printArray(int[] array){
		for(int i = 0; i<array.length; i++){
			TRACE.info(array[i]+" ");
		}
	}

}
