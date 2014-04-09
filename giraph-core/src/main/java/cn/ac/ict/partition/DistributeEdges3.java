package cn.ac.ict.partition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import cn.ac.ict.util.MutableInteger;

public class DistributeEdges3 extends Vertex<LongWritable, VertexValue, IntWritable, MessageValueForDE3> {
	
	private static Logger LOG = Logger.getLogger(DistributeEdges3.class);
	private static Logger TRACE = LOG.getLogger("tracerLogger");
//	private static Logger DEBUG = LOG.getLogger("debugLogger");

	private int[] sequence;
	Map<Integer, Integer> serverToOrder = new HashMap<Integer, Integer>();
	
	@Override
	public void compute(Iterable<MessageValueForDE3> messages) throws IOException {
		int serverNum = getConf().getInt("number.servers", 8);
		sequence = precompute(serverNum, 3);
		if(getSuperstep() == 0){
			MessageValueForDE3 m = new MessageValueForDE3(getId().get(), null);
			sendMessageToAllEdges(m);
		}else if(getSuperstep() == 1){
			Set<Long> edges =  new HashSet<Long>();
			for(Edge<LongWritable, IntWritable> e : getEdges()){
				edges.add(e.getTargetVertexId().get());
			}
			for(MessageValueForDE3 m : messages){
				if(!edges.contains(m.getSrcId())){
					addEdge(EdgeFactory.create(new LongWritable(m.getSrcId()),
            new IntWritable(1)));
				}
			}
			Set<Set<Integer>> paths = new HashSet<Set<Integer>>();
			int order1 = serverToOrder.get(hashToServers(getId().get()));
			int[] servers = getServers(order1, 5);
			TRACE.info(getSuperstep()+"::"+getId().get()+":"+" ");
			printArray(servers, true);
			
			for(Edge<LongWritable, IntWritable> e : getEdges()){
				int targetServer = hashToServers(e.getTargetVertexId().get());
				int orderi = serverToOrder.get(targetServer);
				int[] intersection = getIntersection(order1, orderi, 5, serverNum);
				TRACE.info(getSuperstep()+"::"+getId().get()+":"+e.getTargetVertexId()+" ");
				printArray(getServers(orderi, 5), false);
				TRACE.info("  ;");
				printArray(intersection, true);
				paths = getBestSection(intersection, paths);
			}
			
			int size = Integer.MAX_VALUE;
			List<Set<Integer>> shorestPath = new ArrayList<Set<Integer>>();
			for(Set<Integer> path : paths){
				if(size > path.size()){
					shorestPath.clear();
					size = path.size();
					shorestPath.add(path);
				}else if(size == path.size()){
					shorestPath.add(path);
				}
			}
			TRACE.info(getSuperstep()+"::"+getId().get()+":"+shorestPath.toString()+"\n");
			MessageValueForDE3 message = new MessageValueForDE3(getId().get(), shorestPath);
			sendMessage(getId(), message);
			sendMessageToAllEdges(message);
		} else {
			Map<Set<Integer>, Integer> map = new HashMap<Set<Integer>, Integer>();
			List<Set<Integer>> src = null;
			for(MessageValueForDE3 m : messages){
				if(m.getSrcId() == getId().get()){
					src = m.getServers();
					break;
				}
			}
//			TRACE.info(getSuperstep()+"::"+getId().get() + ": ");
			if(src != null){
				TRACE.info(getSuperstep()+"::"+getId().get() + ": ");
				for(Set<Integer> set : src){
					map.put(set, 0);
					TRACE.info(set.toString()+" ");
				}
				TRACE.info("\n");
			}
			
			boolean isEmpty = map.isEmpty();
			for(MessageValueForDE3 m : messages) {
				TRACE.info(getSuperstep()+"::"+getId().get() + ":"+m.getSrcId()+": ");
				for(Set<Integer> set : m.getServers()){
					if(isEmpty){
						map.put(set, 0);
						continue;
					}
					if(map.get(set) != null){
						map.put(set, map.get(set) + 1);
					}
					TRACE.info(set.toString()+",");
				}
				TRACE.info("\n");
			}
			List<Map.Entry<Set<Integer>, Integer>> mapToList = new ArrayList<Map.Entry<Set<Integer>, Integer>>(map.entrySet());
			Collections.sort(mapToList, new Comparator<Map.Entry<Set<Integer>, Integer>>(){

				@Override
				public int compare(Map.Entry<Set<Integer>, Integer> o1, Map.Entry<Set<Integer>, Integer> o2) {
					// TODO Auto-generated method stub
					return -o1.getValue().compareTo(o2.getValue());
				}
				
			});
			TRACE.info(getSuperstep()+"::"+getId().get() + ": ");
			if(!mapToList.isEmpty()){
//				Random rand = new Random();
//				Set<Integer> bestChoice = mapToList.get(Math.abs(rand.nextInt())%mapToList.size()).getKey();
				Set<Integer> bestChoice = mapToList.get(0).getKey();
				TRACE.info("best: "+bestChoice.toString()+"\n");
			}else{
				TRACE.info(src.get(0).toString()+"\n");
			}
		}
		voteToHalt();
	}
	
	
	private Set<Set<Integer>> getBestSection(int[] stairs, Set<Set<Integer>> paths){
		if(paths.size() == 0){
			for(int i = 0; i < stairs.length; i++){
				Set<Integer> init = new HashSet<Integer>();
				init.add(stairs[i]);
				paths.add(init);
			}
			return paths;
		}
		
		Set<Set<Integer>> copy = new HashSet<Set<Integer>>();
		
		for(Set<Integer> path : paths){
			for(int i = 0; i < stairs.length; i++){
				int stone = stairs[i];
				if(!path.contains(stone)){
					Set<Integer> newPath = new HashSet<Integer>(path);
					newPath.add(stone);
					copy.add(newPath);
				}else{
					copy.add(path);
				}
			}
		}
		return copy;
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
		if(b <  aEnd){
			if(aEnd <= serverNum -1){
				length += aEnd - b +1;
			}else{
				aEnd = aEnd % serverNum;
				length += aEnd + 1;
			}
		}
		
		if(b == aEnd){
			length += 1;
		}
		
		if(bEnd > serverNum - 1){
			bEnd = bEnd % serverNum;
			if(bEnd >= a){
				length += bEnd - a + 1;
			}
			if(aEnd < a){
				length += serverNum - b;
			}
		}
		
		intersection = new int[length];
		int  j = 0;
		if(b <= aEnd){
			for(int i = b; i <= aEnd; i++ ){
				intersection[j++] = sequence[i];
			}
		}else{
			if(aEnd < a){
				for(int i = 0; i <= aEnd; i++ ){
					intersection[j++] = sequence[i];
				}
				for(int i = b; i <= serverNum-1; i++){
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
