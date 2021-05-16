package com.server.packageing;

import com.server.main.ClientConnection;

public interface PackageCallBack {

	/**
	 * Callback function for a specific package.<br>
	 * If defined for a package the client connection will execute this callback when a package of the subscribed type is received.
	 * */
	public void call(DataPackage data, ClientConnection connection);
	
}
