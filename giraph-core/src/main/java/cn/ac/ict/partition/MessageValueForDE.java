package cn.ac.ict.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class MessageValueForDE implements WritableComparable {
	private long id;
	private int[] servers;
	
	public MessageValueForDE(){
		id = 0;
		servers = null;
	}
	
	public MessageValueForDE(long id, int[] servers){
		this.id = id;
		this.servers = servers;
	}
	
	public long getSrcId(){
		return id;
	}
	
	public int[] getServers(){
		return servers;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeLong(id);
		int length = 0;
		if(servers != null){
			length = servers.length;
		}
		out.writeInt(length);
		if(servers != null){
			for(int i : servers){
				out.writeInt(i);
			}
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		id = in.readLong();
		int length = in.readInt();
		servers = new int[length];
		for(int i = 0; i < length; i++){
			servers[i] = in.readInt();
		}
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
