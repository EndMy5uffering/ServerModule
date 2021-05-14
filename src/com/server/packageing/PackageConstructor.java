package com.server.packageing;

public interface PackageConstructor {

	public <T> T build(short length, boolean dynamicLength, byte[] byteDataRaw);

}
