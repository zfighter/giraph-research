package cn.ac.ict.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class MassageValue implements WritableComparable {
	private String label;
	

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
