package com.server.packageing;

public class PackageInfo{
	private byte[] id;
	private short length;
	private boolean dynamicLength;
	private PackageConstructor construct;
	private PackageCallback callback;
	
	public PackageInfo(byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallback packageCallBack) {
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

	public PackageCallback getCallback() {
		return callback;
	}

	public void setCallback(PackageCallback callback) {
		this.callback = callback;
	}
	
	private String formatID() {
		String out = "[";
		int count = 0;
		for(byte b : id) {
			out += b + (count++ < id.length-1 ? " " : "");
		}
		return out+"]";
	}
	
	@Override
	public String toString() {
		return "{ID: " + formatID() + " ,Length: " + length + ", Dynamic: " + dynamicLength + "}";
	}
}
