package com.server.database;

@FunctionalInterface
public interface PackedObject {

	public<T, S> T pack(S data, Class<T> returnType);
	
}
