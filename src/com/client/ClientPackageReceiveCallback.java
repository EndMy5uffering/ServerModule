package com.client;

import com.server.packageing.DataPackage;

@FunctionalInterface
public interface ClientPackageReceiveCallback {

	/**
	 * Callback interface to allow the client connection to inform the server about received packages.
	 * 
	 * @param data received data package form the client connection.
	 * */
	void call(DataPackage data, ClientConnection connection);
	
}
