package cn.ac.ict.partition.xtest;


import org.apache.giraph.bsp.CentralizedServiceWorker;
import org.apache.giraph.conf.GiraphConstants;
import org.apache.giraph.graph.GraphState;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public class DefineHub{
	
	
	
	public static boolean isMyTest(Configuration conf){
		return GiraphConstants.MY_TEST.get(conf);
	}
	
	public void defineHub(){
		
	}
	
	public void getMostEdgesVertices(){
		
	}

}
