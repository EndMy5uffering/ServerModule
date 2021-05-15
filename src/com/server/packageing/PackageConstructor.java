package com.server.packageing;

public interface PackageConstructor {

	/**
	 * General constructor function for a package.<br>
	 * Has to be defined when registering a package so that the client connection can construct custom defined packages.
	 * */
	public DataPackage build(short length, boolean dynamicLength, byte[] byteDataRaw);

}
