package cn.ac.ict.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class MessageValueForHop implements WritableComparable {
	private long id;
	private long newLabel;
	private double score;
	private long numEdges;
	
	public MessageValueForHop(){
		id = 0;
		newLabel = 0;
		score = 0.0;
		numEdges = 0;
	}
	
	public MessageValueForHop(long id, long newLabel, double score, long numEdges){
		this.id = id;
		this.newLabel = newLabel;
		this.score = score;
		this.numEdges = numEdges;
	}
	
	public long getSrcId(){
		return id;
	}
	
	public long getSrcNewLabel(){
		return newLabel;
	}
	
	public double getSrcScore(){
		return score;
	}
	
	public long getNumEdges(){
		return numEdges;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeLong(id);
		out.writeLong(newLabel);
		out.writeDouble(score);
		out.writeLong(numEdges);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		id = in.readLong();
		newLabel = in.readLong();
		score = in.readDouble();
		numEdges = in.readLong();
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
