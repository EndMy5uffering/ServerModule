package com.server.main;

import java.util.ArrayList;
import java.util.List;

import com.server.packageing.DataPackage;

class ClientManager {

	private static List<ClientConnection> connecitons = new ArrayList<>();
	
	public static void submit(ClientConnection connection) {
		connecitons.add(connection);
		connection.enable();
	}
	
	public static void stopClientManager() {
		for(ClientConnection c : connecitons) {
			c.disable();
		}
	}
	
	public synchronized static void removeClient(ClientConnection client) {
		connecitons.remove(client);
	}
	
	public synchronized static void sendToClient(DataPackage data) {
		for(ClientConnection c : connecitons) {
			c.send(data);
		}
	}

}
