package com.server.main;

public class DataPackage {

	private final byte[] byteData;
	private final String stringData;
	
	public DataPackage(byte[] byteData) {
		this.byteData = byteData;
		this.stringData = new String(strip(byteData, (byte)0));
	}
	
	public DataPackage(String data) {
		this.stringData = data;
		byteData = getAsByte(data);
	}
	
	private byte[] shorten(byte[] data) {
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
	
	private byte[] strip(byte[] data, byte b) {
		byte[] out = new byte[data.length];
		for(int i = 0, j = 0; i < data.length; i++) {
			if(data[i] != b) out[j++] = data[i];
		}
		return shorten(out);
	}
	
	private byte[] getAsByte(String s) {
		byte[] out = new byte[s.length()];
		for(int i = 0; i < s.length(); ++i) {
			out[i] = (byte)s.charAt(i);
		}
		return out;
	}

	public byte[] getByteData() {
		return byteData;
	}

	public String getStringData() {
		return stringData;
	}
	
}
