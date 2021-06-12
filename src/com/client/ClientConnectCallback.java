package com.client;

@FunctionalInterface
public interface ClientConnectCallback {

	public void call(ClientConnection con);
	
}
