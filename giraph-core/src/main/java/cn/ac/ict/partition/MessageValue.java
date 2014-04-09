package cn.ac.ict.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class MessageValue implements WritableComparable {
	private long id;
	private long newLabel;
	
	public MessageValue(){
		id = 0;
		newLabel = 0;
	}
	
	public MessageValue(long id, long newLabel){
		this.id = id;
		this.newLabel = newLabel;
	}
	
	public long getSrcId(){
		return id;
	}
	
	public long getSrcNewLabel(){
		return newLabel;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeLong(id);
		out.writeLong(newLabel);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		id = in.readLong();
		newLabel = in.readLong();
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
