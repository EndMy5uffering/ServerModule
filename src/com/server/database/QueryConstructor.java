package com.server.database;

@FunctionalInterface
public interface QueryConstructor {
	
	public String construct(QueryObject q);
	
}
