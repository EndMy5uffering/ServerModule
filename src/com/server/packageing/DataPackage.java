package com.server.packageing;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.logger.Level;
import com.server.main.Server;
import com.server.packageing.annotations.DataPackageConstructor;


public class DataPackage{

	public final static short IDLENGTH = 2;
	
	private byte[] id;
	private byte[] byteDataRaw;
	private short length;
	private boolean dynamicLength = false;
	
	public DataPackage(short length, boolean dynamicLength, byte[] byteDataRaw) {
		this(new byte[] {0x0, 0x0}, length, dynamicLength, byteDataRaw);
	}

	@DataPackageConstructor(ID = true, LENGTH = true, DYNAMIC = true, DATA = true)
	public DataPackage(byte[] id, short length, boolean dynamicLength, byte[] byteDataRaw) {
		this.byteDataRaw = byteDataRaw;
		this.length = length;
		this.dynamicLength = dynamicLength;
		this.id = id;
	}
	
	/**
	 * Returns the ID length used by all packages.<br>
	 * 
	 * Every package uses the first [IDLENGTH] byte as id to be recognized by the package manager.
	 * 
	 * */
	public static short getIDLENGTH() {
		return IDLENGTH;
	}

	/**
	 * This will pack all the relevant information about the package into one byte array that can be send via a socket connection.
	 * <br><br>
	 * All packages consist of:<br><br>
	 * [IDLENGTH] bytes for the ID<br>
	 * [Length] bytes for the package length when using dynamic length<br>
	 * [Length] for static length are the bytes of [byteDataRaw]<br>
	 * [byteDataRaw] bytes of raw byte data<br>
	 * <br>
	 * Sample dynamic length:<br>
	 * <pre> ID	  Length	  RawData
	 * [0x0 0x0][0x0 0x0 0x0 0x1][0x0]</pre>
	 * <br>
	 * Sample static length:
	 * <pre> ID	 RawData
	 * [0x0 0x0][0x0]</pre>
	 * 
	 * @return byte[]
	 * */
	public byte[] pack() {
		byte[] l = new byte[0];
		if(dynamicLength) {
			l = DataPackage.getByteArrayFromInt(this.byteDataRaw.length, this.length);
		}
		return concatAll(this.id, l, this.byteDataRaw);
	}
	
	/**
	 * Cuts of all trailing 0 bytes.
	 * */
	public static byte[] shorten(byte[] data) {
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
	
	/**
	 * Removes all bytes equal to <b>b</b> and cuts of all tailing 0 bytes.
	 */
	public static byte[] strip(byte[] data, byte b) {
		byte[] out = new byte[data.length];
		for(int i = 0, j = 0; i < data.length; i++) {
			if(data[i] != b) out[j++] = data[i];
		}
		return DataPackage.shorten(out);
	}
	
	/**
	 * Converts an integer to a byte array of length <b>arrayLength</b>
	 * 
	 * @param i Integer to be converted to a byte array
	 * @param arrayLength length of the return array
	 * */
	@Deprecated
	public static byte[] getByteArrayFromInt(int i, int arrayLength) {
		ByteBuffer buffer = ByteBuffer.allocate(arrayLength);
		buffer.putInt(i);
		return buffer.array();
	}
	
	/**
	 * Converts a byte array to an integer.<br>
	 * If the byte array is larger then 4 byte information might be lost.<br>
	 * If the byte array is smaller then 1 or null the function will return <b>-1</b>
	 * */
	public static long getFromByte(byte[] b) {
		ByteBuffer buffer = ByteBuffer.wrap(b);
		if(b == null) return -1;
		if(b.length < 4 && b.length > 1) {
			return buffer.getShort();
		}else if(b.length > 3) {
			return buffer.getInt();
		}else if(b.length == 1) {
			return buffer.get();
		}
		return -1;
	}
	
	/**
	 * Concats the given byte arrays to one large array.
	 * */
	public static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		  System.arraycopy(second, 0, result, first.length, second.length);
		  return result;
	}
	
	/**
	 * Concats the given byte arrays to one large array.
	 * */
	@SafeVarargs
	public static byte[] concatAll(byte[] first, byte[]... rest) {
		  int totalLength = first.length;
		  for (byte[] array : rest) {
		    totalLength += array.length;
		  }
		  byte[] result = Arrays.copyOf(first, totalLength);
		  int offset = first.length;
		  for (byte[] array : rest) {
		    System.arraycopy(array, 0, result, offset, array.length);
		    offset += array.length;
		  }
		  return result;
	}
	
	/**
	 * This function will pad the given array to the new length.<br>
	 * The given byte array will be written from offset to array.length in the output array.<br>
	 * 
	 * @param array The input array that will be padded.
	 * @param length The length of the output array.
	 * @param offset Number of bytes the input array will be offset.
	 * */
	public static byte[] pad(byte[] array, int length, int offset) {
		byte[] out = new byte[length];
		
		for(int i = 0; i < array.length; i++) {
			out[i+offset] = array[i];
		}
		
		return out;
	}
	
	/**
	 * Returns a byte array with the bytes from <b>a</b> in the range <b>[from, to[</b><br>
     * The range goes from <b>from</b> (incluseive) to <b>to</b> (exclusive)<br>
     * <br>
     * Example:<br>
     * a = [0, 1, 2, 3, 4, 5, 6, 7, 8 ,9]<br>
     * from = 3 | to = 8<br>
     * output: [3, 4, 5, 6, 7]
	 * */
	public static byte[] getArrayFromTo(byte[] a, int from, int to) {
		byte[] out = new byte[to - from];
		
		for(int i = from; i < to; i++) {
			if(i >= a.length) return new byte[] {};
			out[i-from] = a[i];
		}
		
		return out;
	}
	
	public static byte[] toByte(float f) {
		ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
		buffer.putFloat(f);
		return buffer.array();
	}
	
	public static byte[] toByte(double d) {
		ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
		buffer.putDouble(d);
		return buffer.array();
	}
	
	public static byte[] toByte(String s) {
		return s.getBytes();
	}
	
	public static byte[] toByte(int i) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(i);
		return buffer.array();
	}
	
	public static byte[] toByte(long l) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(l);
		return buffer.array();
	}
	
	public static byte[] toByte(short s) {
		ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
		buffer.putShort(s);
		return buffer.array();
	}
	
	public static float toFloat(byte[] d) {
		return ByteBuffer.wrap(d).asFloatBuffer().get();
	}
	
	public static double toDouble(byte[] d) {
		ByteBuffer buffer = ByteBuffer.wrap(d);
		return buffer.asDoubleBuffer().get();
	}
	
	public static String toString(byte[] d) {
		return new String(d);
	}
	
	public static int toInt(byte[] d) {
		if(d.length != Integer.BYTES) 
			throw new IllegalArgumentException("Can not convert byte array of size: " + d.length + " to int. To convert to int the byte array needs a size of: " + Integer.BYTES);
		ByteBuffer buffer = ByteBuffer.wrap(d);
		return buffer.getInt();
	}
	
	public static long toLong(byte[] d) {
		if(d.length != Long.BYTES) 
			throw new IllegalArgumentException("Can not convert byte array of size: " + d.length + " to long. To convert to long the byte array needs a size of: " + Long.BYTES);
		ByteBuffer buffer = ByteBuffer.wrap(d);
		return buffer.getLong();
	}
	
	public static short toShort(byte[] d) {
		if(d.length != Short.BYTES) 
			throw new IllegalArgumentException("Can not convert byte array of size: " + d.length + " to short. To convert to short the byte array needs a size of: " + Short.BYTES);
		ByteBuffer buffer = ByteBuffer.wrap(d);
		return buffer.getShort();
	}
	
	@Override
	public String toString() {
		String out = "";
		for(byte b : this.pack()) {
			out += b + (b > 31 && b < 127 ? "(" + (char)b + ") ": " ");
		}
		return out;
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

	/**
	 * Sets the package id.<br>
	 * the package id has to be <b>Datapackage.IDLENGTH</b> byte long.<br>
	 * */
	public void setId(byte[] id) {
		this.id = id;
	}

	public void setByteDataRaw(byte[] byteDataRaw) {
		this.byteDataRaw = byteDataRaw;
	}
	
	public static void printByteArray(byte[] data) {
		String out = "Byte("+data.length+"): ";
		for(int i = 0; i < data.length; i++) {
			out += data[i]+" ";
		}
		Server.getLogger().log(Level.INFO, out);
	}

}
