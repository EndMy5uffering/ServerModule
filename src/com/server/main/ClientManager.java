package com.server.main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.logger.Level;
import com.server.packageing.DataPackage;

class ClientManager {

	private Map<String, UUID> NamedConnection = new HashMap<>();
	private Map<UUID, ClientConnection> connecitons = new HashMap<>();
	private Server server;
	
	
	public ClientManager(Server server) {
		this.server = server;
	}
	
	/**
	 * Adds the client to the manager list <b>and</b> enables (starts) the client thread.
	 * 
	 * @param connection
	 * */
	public void submit(ClientConnection connection) {
		connecitons.put(connection.getId(), connection);
		connection.enable();
		if(connection.getServer().getClientConnectCallback() != null)
			connection.getServer().getClientConnectCallback().call(connection);
	}
	
	/**
	 * Links a connection with a given name.<br>
	 * If a connection is linked with a name you can get the connection back with getConnection(NAME).
	 * 
	 * @throws NullPointerException When id == null or name == null or name == ""
	 * @throws IllegalArgumentException When a client has already been registered with the given name.
	 * 
	 * @param id UUID of the connection. You can get the id from the connection with the getId() function.
	 * @param name The name you want to give you connection for better access.
	 * */
	public void setClientName(UUID id, String name) {
		if(id == null || name == null || name == "")
			throw new NullPointerException("ID and Name can not be null!");
		if(NamedConnection.get(name) != null)
			throw new IllegalArgumentException("Connection with the given name already exists!");
		NamedConnection.put(name, id);
	}
	
	/**
	 * Returns the client connection belonging to the given id.<br>
	 * If not connection is registered under the given id the function will return <b>null</b>.
	 * 
	 * @param id The UUID of the connection.
	 * */
	public ClientConnection getClientConnection(UUID id) {
		return this.connecitons.get(id);
	}
	
	/**
	 * Returns the client connection belonging to the given name.<br>
	 * If not connection is registered under the given name the function will return <b>null</b>.
	 * 
	 * @param name The name the connection is registered under.
	 * */
	public ClientConnection getClientConnection(String name) {
		UUID id = NamedConnection.get(name);
		if(id == null) return null;
		return this.connecitons.get(id);
	}
	
	/**
	 * Disables all current client connections.
	 * For all client connections the disable function is called.
	 * 
	 * */
	public void stopClients() {
		for(ClientConnection c : connecitons.values()) {
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
		connecitons.remove(client.getId());
		if(client.getConnectionName() != "" && client.getConnectionName() != null) NamedConnection.remove(client.getConnectionName());
		server.getLogger().log(Level.INFO, "Client connection discarded for: " + client.getSocket().getInetAddress().getHostAddress());
	}
	
	
	/**
	 * Sends a package to the all current client connections.
	 * 
	 * @param data A package of type DataPackage
	 * */
	public synchronized void sendToClient(DataPackage data) {
		for(ClientConnection c : connecitons.values()) {
			c.send(data);
		}
	}

}
