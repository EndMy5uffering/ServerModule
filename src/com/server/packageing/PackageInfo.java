package com.server.packageing;

public class PackageInfo{
	private byte[] id;
	private short length;
	private boolean dynamicLength;
	private PackageConstructor construct;
	
	public PackageInfo(byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		this.id = id;
		this.length = length;
		this.dynamicLength = dynamicLength;
	}

	public byte[] getId() {
		return id;
	}

	public short getLength() {
		return length;
	}

	public boolean isDynamicLength() {
		return dynamicLength;
	}

	public PackageConstructor getConstruct() {
		return construct;
	}
}
