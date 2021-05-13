package com.server.main;

import com.logger.Level;

public class ServerMain {

	public static void main(String... args) {
		Server s = new Server(25565);
//		s.setPrintMode(PrintMode.Debug);
		
		s.setDefaultClientCallBack((data) -> {
			Server.logger.log(Level.INFO, data.getStringData());
		});
		
		try {
			s.start();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		//s.stopServer();
	}
	
}
