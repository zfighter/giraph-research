package cn.ac.ict.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.WritableComparable;

public class MessageValueForDE3 implements WritableComparable {
	private long id;
	private List<Set<Integer>> servers;
	
	public MessageValueForDE3(){
		id = 0;
		servers = new ArrayList<Set<Integer>>();
	}
	
	public MessageValueForDE3(long id, List<Set<Integer>> servers){
		this.id = id;
		this.servers = servers;
	}
	
	public long getSrcId(){
		return id;
	}
	
	public List<Set<Integer>> getServers(){
		return servers;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeLong(id);
		int length = 0;
		if(servers != null){
			length = servers.size();
		}
		out.writeInt(length);
		if(servers != null){
			for(Set<Integer> s : servers){
				out.writeInt(s.size());
				for(int i : s){
					out.writeInt(i);
				}
			}
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		id = in.readLong();
		int length = in.readInt();
		servers = new ArrayList<Set<Integer>>();
		for(int i = 0; i < length; i++){
			int setLen = in.readInt();
			Set<Integer> s = new HashSet<Integer>();
			for(int j = 0; j < setLen; j++){
				s.add(in.readInt());
			}
			servers.add(s);
		}
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
