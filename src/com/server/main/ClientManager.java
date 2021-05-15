package com.server.main;

import java.util.ArrayList;
import java.util.List;

import com.server.packageing.DataPackage;

class ClientManager {

	private List<ClientConnection> connecitons = new ArrayList<>();
	
	public ClientManager() {
		
	}
	
	/**
	 * Adds the client to the manager list <b>and</b> enables (starts) the client thread.
	 * 
	 * @param connection
	 * */
	public void submit(ClientConnection connection) {
		connecitons.add(connection);
		connection.enable();
	}
	
	/**
	 * Disables all current client connections.
	 * For all client connections the disable function is called.
	 * 
	 * */
	public void stopClients() {
		for(ClientConnection c : connecitons) {
			c.disable();
		}
	}
	
	/**
	 * Removes client form manager list.<br>
	 * When removed a client can no longer be managed by the server and will not receive new messages from the server.
	 * 
	 * @param client
	 * */
	public synchronized void removeClient(ClientConnection client) {
		connecitons.remove(client);
	}
	
	
	/**
	 * Sends a package to the all current client connections.
	 * 
	 * @param data A package of type DataPackage
	 * */
	public synchronized void sendToClient(DataPackage data) {
		for(ClientConnection c : connecitons) {
			c.send(data);
		}
	}

}
