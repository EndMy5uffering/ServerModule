package com.server.packageing;

import com.server.main.ClientConnection;

public interface UnknownPackageHandling {
	
	public void handle(byte[] packageID, ClientConnection con);
	
}
