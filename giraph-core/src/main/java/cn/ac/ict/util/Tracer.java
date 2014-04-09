package cn.ac.ict.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Writable;

import com.google.common.collect.Maps;

public abstract class Tracer implements Writable{
	
	private Map<Long, List<Long>> trace = Maps.newHashMap();
	

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
