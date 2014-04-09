package cn.ac.ict.partition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import cn.ac.ict.util.MutableInteger;

public class DistributeEdges2 extends Vertex<LongWritable, VertexValue, IntWritable, MessageValueForDE> {
	
	private static Logger LOG = Logger.getLogger(DistributeEdges2.class);
	private static Logger TRACE = LOG.getLogger("tracerLogger");
//	private static Logger DEBUG = LOG.getLogger("debugLogger");

	private int[] sequence;
	Map<Integer, Integer> serverToOrder = new HashMap<Integer, Integer>();
	
	@Override
	public void compute(Iterable<MessageValueForDE> messages) throws IOException {
		int serverNum = getConf().getInt("number.servers", 8);
		sequence = precompute(serverNum, 3);
		if(getSuperstep() == 0){
			Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
			int order1 = serverToOrder.get(hashToServers(getId().get()));
			int[] servers = getServers(order1, 5);
			TRACE.info(getSuperstep()+"::"+getId().get()+":"+" ");
			printArray(servers, true);
			for(int server : servers){
				counts.put(server, 0);
			}
			for(Edge<LongWritable, IntWritable> e : getEdges()){
				int targetServer = hashToServers(e.getTargetVertexId().get());
				int orderi = serverToOrder.get(targetServer);
				int[] intersection = getIntersection(order1, orderi, 5, serverNum);
				TRACE.info(getSuperstep()+"::"+getId().get()+":"+e.getTargetVertexId()+" ");
				printArray(getServers(orderi, 5), false);
				TRACE.info("  ;");
				printArray(intersection, true);
				for(int s : intersection){
					counts.put(s, counts.get(s) + 1);
				}
			}
			List<Map.Entry<Integer, Integer>> mapToList = new ArrayList<Map.Entry<Integer, Integer>>(counts.entrySet());
			Collections.sort(mapToList, new Comparator<Map.Entry<Integer, Integer>>(){

				@Override
				public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
					// TODO Auto-generated method stub
					return -o1.getValue().compareTo(o2.getValue());
				}
				
			});
			//fill servers with ordered list
			int i = 0;
			for(Map.Entry<Integer, Integer> e : mapToList){
				servers[i++] = e.getKey();
			}
			TRACE.info(getSuperstep()+"::"+getId().get()+":");
			printArray(servers, true);
			MessageValueForDE message = new MessageValueForDE(getId().get(), servers);
			sendMessage(getId(), message);
			sendMessageToAllEdges(message);
		}else{
			int[] src = null;
			for(MessageValueForDE m : messages){
				if(m.getSrcId() == getId().get()){
					src = m.getServers();
					break;
				}
			}
			TRACE.info(getSuperstep()+"::"+getId().get() + ": ");
			printArray(src, true);
			for(MessageValueForDE m : messages){
				if(m.getSrcId() != getId().get()){
					int[] trg = m.getServers();
					for(int s : src){
						for(int t : trg){
							if(s == t){
								TRACE.info(getSuperstep()+"::"+m.getSrcId()+" "+t+"\n");
								break;
							}
						}
					}
				}
			}
		}
		voteToHalt();
	}
	
	private int[] getServers(int begin, int length){
		int[] res = new int[length];
		if(length > sequence.length || sequence == null || sequence.length == 0){
			return null;
		}
		for(int i = 0, j = 0; i < length; i++){
			if(begin + i < 8){
				res[j++] = sequence[begin+i];
			}else{
				res[j++] = sequence[(begin + i) % 8];
			}
		}
		return res;
	}
	
	private int[] getIntersection(int a, int b, int num, int serverNum){
		if(sequence == null) return null;
		int[] intersection = null;
		int length = 0;
		if(a > b){
			int temp = a;
			a = b;
			b = temp;
		}
		int aEnd = a + num - 1, bEnd = b + num - 1;
		if(b <=  aEnd){
			if(aEnd <= serverNum -1){
				length += aEnd - b +1;
			}else{
				aEnd = aEnd % serverNum;
				length += aEnd + 1;
			}
		}
		
		if(bEnd > serverNum - 1){
			bEnd = bEnd % serverNum;
			if(bEnd >= a){
				length += bEnd - a + 1;
			}
		}
		
		intersection = new int[length];
		int j = 0;
		if(b <= aEnd){
			for(int i = b; i <= aEnd; i++ ){
				intersection[j++] = sequence[i];
			}
		}else{
			if(aEnd < a){
				for(int i = 0; i <= aEnd; i++ ){
					intersection[j++] = sequence[i];
				}
			}
			
		}
		
		if(bEnd >= a && bEnd < b){
			for(int i = a; i <= bEnd; i++ ){
				intersection[j++] = sequence[i];
			}
		}
		
		return intersection;
	}
	
	private int[] precompute(int serverNum, int inter){
		int[] res = new int[serverNum];
		res[0] = 0;
		serverToOrder.put(0, 0);
		for(int i = 1; i < serverNum; i++){
			res[i] = (res[i-1] + inter) % serverNum;
			serverToOrder.put(res[i], i);
		}
		return res;
	}
	
	private int hashToServers(long vertex) throws IOException{
		long base = getConf().getLong("base.hash", 5);
		int serversNum = getConf().getInt("number.servers", 4);
		long hashcode = hash(vertex, base);
		int server = (int)(hashcode % serversNum);
		return server;
	}
	
	private long hash(long vertexId, long base){
		if(base == 0)
			base = 5381l;
		long hash = base;
		hash = ((hash << 5) + hash) + vertexId;
		return hash;
	}
	
	private void printArray(int[] array, boolean add){
		for(int i = 0; i<array.length; i++){
			TRACE.info(array[i]+" ");
		}
		if(add){
			TRACE.info("\n");
		}
	}

}
