package com.client;

@FunctionalInterface
public interface ClientTimeOutCallback {

	public void call(ClientConnection con);
	
}
