package com.client;

import java.util.HashMap;
import java.util.Map;

import com.logger.Level;
import com.server.main.Server;
import com.server.main.SessionID;
import com.server.packageing.DataPackage;

public class ClientManager {

	private Map<String, SessionID> NamedConnection = new HashMap<>();
	private Map<SessionID, ClientConnection> connecitons = new HashMap<>();
	
	
	public ClientManager() {
		
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
	 * @throws IllegalArgumentException When there is no client with the given id.
	 * @throws IllegalAccessException When the connection was already named.
	 * @param id SessionID of the connection. You can get the id from the connection with the getId() function.
	 * @param name The name you want to give you connection for better access.
	 * */
	public void setClientName(SessionID id, String name) {
		if(id == null || name == null || name == "")
			throw new NullPointerException("ID and Name can not be null!");
		if(NamedConnection.get(name) != null)
			throw new IllegalArgumentException("Connection with the given name already exists!");
		if(this.connecitons.get(id) == null)
			throw new IllegalArgumentException("No client connection for: " + id);
		NamedConnection.put(name, id);
		this.connecitons.get(id).setConnectionName(name);
	}
	
	/**
	 * Returns the client connection belonging to the given id.<br>
	 * If not connection is registered under the given id the function will return <b>null</b>.
	 * 
	 * @param sessionID The SessionID of the connection.
	 * */
	public ClientConnection getClientConnection(SessionID sessionID) {
		return this.connecitons.get(sessionID);
	}
	
	/**
	 * Returns the client connection belonging to the given id.<br>
	 * If not connection is registered under the given id the function will return <b>null</b>.
	 * 
	 * @param sessionID The SessionID as string of the connection.
	 * */
	public ClientConnection getClientConnection(String sessionID) {
		return this.getClientConnection(new SessionID(sessionID));
	}
	
	/**
	 * Returns the client connection belonging to the given name.<br>
	 * If not connection is registered under the given name the function will return <b>null</b>.
	 * 
	 * @param name The name the connection is registered under.
	 * */
	public ClientConnection getClientConnectionByName(String name) {
		SessionID id = NamedConnection.get(name);
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
	 * When removed a client can no longer be managed by the server and will not receive new messages from the server.<br>
	 * When the client is disabled it will call this function automatically to remove itself from the manager.
	 * 
	 * @param client
	 * */
	public synchronized void removeClient(ClientConnection client) {
		connecitons.remove(client.getId());
		if(client.getConnectionName() != "" && client.getConnectionName() != null) NamedConnection.remove(client.getConnectionName());
		Server.getLogger().log(Level.INFO, "Client connection discarded for: " + client.getSocket().getInetAddress().getHostAddress());
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
