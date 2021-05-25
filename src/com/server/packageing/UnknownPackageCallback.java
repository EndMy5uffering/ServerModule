package com.server.packageing;

import com.server.main.ClientConnection;

public interface UnknownPackageCallback {
	
	/**
	 * Callback function for the client connection when receiving an unknown package in the current context.<br>
	 * Packages are unknown when the read package id could not be found in the current package handler.<br>
	 * 
	 * @param packageID The id that was read by the client connection
	 * @param con The client connection that read the faulty package id.
	 * */
	public void handle(byte[] packageID, ClientConnection con);
	
}
