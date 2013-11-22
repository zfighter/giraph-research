package cn.ac.ict.util;

public interface Mutable<T> {
	
	public void set(T value);
	
	public T get();

}
