package cn.ac.ict.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.WritableComparable;

public class VertexValue implements WritableComparable {
	private long label;
	private long value;
	
	//this list is just used for trace the mutation of label
	private List<Long> labelMutations = new ArrayList<Long>();
	
	public VertexValue(){
		label = 0;
		value = 0;
		labelMutations.add(label);
	}
	
	public VertexValue(long label, long value){
		this.label = label;
		this.value = value;
		this.labelMutations.add(label);
	}


	public long getLabel() {
		return label;
	}

	public void setLabel(long label) {
		this.label = label;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
	
	public void addTrace(long newLabel){
		labelMutations.add(newLabel);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(label);
		out.writeLong(value);
		out.write(labelMutations.size());
		for(int i = 0; i < labelMutations.size(); i++){
			out.writeLong(labelMutations.get(i));
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.label = in.readLong();
		this.value = in.readLong();
		if(labelMutations == null){
			labelMutations = new ArrayList<Long>();
		}
		int size = in.readInt();
		for(int i = 0; i < size; i++){
			labelMutations.add(in.readLong());
		}
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String toString(){
		String str = label + ":" + value + ";";
		for(int i = 0; i < labelMutations.size(); i++){
			str += labelMutations.get(i) + ",";
		}
		return str;
	}

}
