package com.server.main;

import com.logger.Level;
import com.logger.PrintMode;
import com.server.basepackages.MessagePackage;

public class ServerMain {

	public static void main(String... args) {
	
		Server s = new Server(25565, (data) -> {
			if(data instanceof MessagePackage) {
				Server.logger.log(Level.INFO, ((MessagePackage)data).getMessage());
			}
		});

		s.start();
		s.setPrintMode(PrintMode.Debug);
		
		//s.stopServer();
	}
	
}