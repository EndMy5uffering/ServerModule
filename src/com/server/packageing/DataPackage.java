package com.server.packageing;

import java.nio.ByteBuffer;

public class DataPackage implements PackageConstructor{

	public final static short IDLENGTH = 2;
	
	private byte[] id;
	private byte[] byteDataRaw;
	private short length;
	private boolean dynamicLength = false;
	
	public DataPackage(short length, boolean dynamicLength, byte[] byteDataRaw) {
		this.byteDataRaw = byteDataRaw;
		this.length = length;
		this.dynamicLength = dynamicLength;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T build(short length, boolean dynamicLength, byte[] byteDataRaw) {
		return (T) new DataPackage(length, dynamicLength, byteDataRaw);
	}
	
	public static short getIDLENGTH() {
		return IDLENGTH;
	}

	public byte[] pack() {
		//TODO: DO stuff
		byte[] l = DataPackage.getLengthFromInt(this.length, this.length);
		
		byte[] out = new byte[IDLENGTH + l.length + this.byteDataRaw.length];
		for(int i = 0; i < IDLENGTH; ++i) {
			out[i] = id[i];
		}
		for(int i = IDLENGTH; i < l.length + IDLENGTH; ++i) {
			out[i] = l[i-IDLENGTH];
		}
		for(int i = IDLENGTH+this.length, j = 0; i < out.length; ++i) {
			out[i] = this.byteDataRaw[j++];
		}
		return out;
	}
	
	public byte[] shorten(byte[] data) {
		int to = 0;
		for(int i = data.length-1; i >= 0; --i) {
			if(data[i] > 0) {
				to = i+1;
				break;
			}
		}
		
		byte[] out = new byte[to];
		
		for(int i = 0; i < to; i++) {
			out[i] = data[i];
		}
		return out;
	}
	
	public byte[] strip(byte[] data, byte b) {
		byte[] out = new byte[data.length];
		for(int i = 0, j = 0; i < data.length; i++) {
			if(data[i] != b) out[j++] = data[i];
		}
		return shorten(out);
	}
	
	public byte[] getAsByte(String s) {
		byte[] out = new byte[s.length()];
		for(int i = 0; i < s.length(); ++i) {
			out[i] = (byte)s.charAt(i);
		}
		return out;
	}
	
	public static byte[] getLengthFromInt(int l, int arrayLength) {
		ByteBuffer buffer = ByteBuffer.allocate(arrayLength);
		buffer.putInt(l);
		return buffer.array();
	}
	
	public static int getLengthFromByte(byte[] b) {
		ByteBuffer buffer = ByteBuffer.wrap(b);
		return buffer.getInt();
	}
	
	public byte[] getByteData() {
		return byteDataRaw;
	}

	public byte[] getByteDataRaw() {
		return byteDataRaw;
	}

	public short getLength() {
		return length;
	}

	public boolean isDynamicLength() {
		return dynamicLength;
	}

	public byte[] getId() {
		return id;
	}

	public void setId(byte[] id) {
		this.id = id;
	}

	public void setByteDataRaw(byte[] byteDataRaw) {
		this.byteDataRaw = byteDataRaw;
	}

	
}
