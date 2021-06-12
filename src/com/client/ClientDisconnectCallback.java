package com.client;

@FunctionalInterface
public interface ClientDisconnectCallback {

	public void call(ClientConnection con);
	
}
