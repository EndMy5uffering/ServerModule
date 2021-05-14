package com.server.packageing;

public class PackageInfo{
	private byte[] id;
	private short length;
	private boolean dynamicLength;
	private PackageConstructor construct;
	private PackageCallBack callback;
	
	public PackageInfo(byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallBack packageCallBack) {
		this.id = id;
		this.length = length;
		this.dynamicLength = dynamicLength;
		this.construct = construct;
		this.callback = packageCallBack;
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

	public PackageCallBack getCallback() {
		return callback;
	}

	public void setCallback(PackageCallBack callback) {
		this.callback = callback;
	}
}
