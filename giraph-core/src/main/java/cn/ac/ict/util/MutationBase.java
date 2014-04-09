package cn.ac.ict.util;

public class MutationBase<T> implements Mutable<T> {
	
	private T value;
	
	public MutationBase(T value){
		this.value = value;
	}

	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

}
