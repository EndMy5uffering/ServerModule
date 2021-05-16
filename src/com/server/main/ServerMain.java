package com.server.main;

import com.logger.PrintMode;

public class ServerMain {

	public static void main(String... args) {
	
		Server s = new Server(25565);
		s.start();
		s.setPrintMode(PrintMode.Debug);
		
		//s.stopServer();
	}
	
}