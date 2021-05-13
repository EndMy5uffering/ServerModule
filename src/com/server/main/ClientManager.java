package com.server.main;

import java.util.ArrayList;
import java.util.List;

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
	
	public static void sendToClient() {
		
	}

}
