package com.server.packageing;

@FunctionalInterface
public interface PackageConstructor {

	/**
	 * General constructor function for a package.<br>
	 * Has to be defined when registering a package so that the client connection can construct custom defined packages.
	 * */
	public DataPackage build(byte[] id, short length, boolean dynamicLength, byte[] byteDataRaw);

}
