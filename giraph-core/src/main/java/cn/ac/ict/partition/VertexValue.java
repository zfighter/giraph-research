package cn.ac.ict.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class VertexValue implements WritableComparable {
	private String label;
	private double value;
	
	public VertexValue(String label, double value){
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeChars(label);
		out.writeDouble(value);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.label = in.readLine();
		this.value = in.readDouble();
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
