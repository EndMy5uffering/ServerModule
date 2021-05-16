package com.server.main;


import java.util.Set;

import com.logger.Level;
import com.server.packageing.DefaultPackageManager;
import com.server.packageing.PackageInfo;

public class ServerMain {

	public static void main(String... args) {
	
		Server s = new Server(25565);
		Set<PackageInfo> out = s.getPackageRegistrationManager().getAllPackagesForManager(DefaultPackageManager.class);
		if(out != null)
			s.getLogger().log(Level.INFO, "OUT: " + out.size());
		
		s.start();
		
		//s.stopServer();
	}
	
}