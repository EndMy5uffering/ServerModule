package com.server.packageing;

import com.server.main.Server;

public class DefaultPackageManager extends PackageManager{

	public DefaultPackageManager(Server server, Class<? extends PackageManager> type) {
		super(server, type);
	}

}
