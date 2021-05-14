package com.server.main;

import com.logger.Level;
import com.logger.PrintMode;
import com.server.basepackages.MessagePackage;
import com.server.packageing.PackageConstructor;
import com.server.packageing.PackageManager;


public class ServerMain {

	public static void main(String... args) {
		PackageManager.register(MessagePackage.ID, MessagePackage.PACK_LENGTH, true, new PackageConstructor() {
			@SuppressWarnings("unchecked")
			@Override
			public MessagePackage build(short length, boolean dynamicLength, byte[] byteDataRaw) {
				return new MessagePackage(dynamicLength, byteDataRaw);
			}
		});
		Server s = new Server(25565);
		s.setPrintMode(PrintMode.Debug);
		
		s.setDefaultClientCallBack((data) -> {
			Server.logger.log(Level.INFO, new String(data.getByteData()));
		});
		
		try {
			s.start();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		
		//s.stopServer();
	}
	
}
