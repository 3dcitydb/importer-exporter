package de.tub.citydb.filter;

public interface Filter<T> {
	public boolean isActive();
	public boolean filter(T element);
}
