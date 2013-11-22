package cn.ac.ict.util;

public class MutableInteger implements Mutable<Integer> {
	
	private int value; 

	public MutableInteger(int value){
		this.value = value;
	}
	
	@Override
	public void set(Integer value) {
		this.value = value;
	}

	@Override
	public Integer get() {
		return this.value;
	}

}
