package com.server.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONString {

	private Map<String, Object> valueMap = new HashMap<>();
	private static String illegalArg = "Could not parse string";
	
	public JSONString() {
		
	}
	
	public void add(String key, String value) {
		valueMap.put(key, value);
	}
	
	public void add(String key, String[] value) {
		this.valueMap.put(key, value);
	}
	
	public void add(String key, JSONString jstring) {
		this.valueMap.put(key, jstring);
	}
	
	public void add(String key, JSONString[] jstrings) {
		this.valueMap.put(key, jstrings);
	}
	
	public void add(String key, Integer value) {
		this.valueMap.put(key, value);
	}
	
	public void add(String key, Integer[] value) {
		this.valueMap.put(key, value);
	}
	
	public void add(String key, Double value) {
		this.valueMap.put(key, value);
	}
	
	public void add(String key, Double[] value) {
		this.valueMap.put(key, value);
	}
	
	public void add(String key, Object o) {
		this.valueMap.put(key, o);
	}
	
	public static JSONString parse(String jsonString) throws IllegalArgumentException{		
		JSONString jString = new JSONString();
		JSONWrapper wrapper = new JSONWrapper(jsonString.replace("\n", ""));
		if(wrapper.next() == '{') {
			wrapper.next();
			jString = parseJsonObject(wrapper);
		}else {
			throw new IllegalArgumentException(illegalArg + " : " + wrapper.current());
		}
		
		return jString;
	}
	
	private static JSONString parseJsonObject(JSONWrapper w) {
		JSONString jString = new JSONString();
		
		boolean readKey = false;
		boolean readEndOfObject = false;
		
		String key = "";
		
		while(w.hasNext() && !readEndOfObject) {
			char current = w.next();
			switch(current) {
			case ',':
				readKey = false;
				key = "";
				break;
			case ':':
				if(!readKey) {
					throw new IllegalArgumentException(illegalArg + " " + current + readRest(w));
				}
				break;
			case ' ':
				break;
			case '"':
				if(readKey) {
					jString.add(key, parseString(w));
				}else {
					key = parseString(w);
					readKey = true;
				}
				break;
			case '}':
				readEndOfObject = true;
				break;
			case '{':
				if(readKey) {
					jString.add(key, parseJsonObject(w));
				}else {
					throw new IllegalArgumentException(illegalArg + " " + current + readRest(w));
				}
				break;
			case '[':
				if(readKey) {
					Object readObjects = parseArray(w);
					jString.add(key, readObjects);
				}else {
					throw new IllegalArgumentException(illegalArg + " " + current + readRest(w));
				}
				break;
			default:
				try {
					if(readKey) {
						String number = readNumber(w, current);
						
						if(number != "") {
							if(number.contains(".")) {
								Double d = Double.parseDouble(number);
								jString.add(key, d);
							}else {
								Integer i = Integer.parseInt(number);
								jString.add(key, i);
							}
						}
						break;
					}
				} catch (Exception e) {
					throw new IllegalArgumentException(illegalArg + " " + current + readRest(w));
				}
			}
		}
		
		return jString;
	}
	
	private static String parseString(JSONWrapper w) {
		String readString = "";
		boolean foundEnd = false;
		
		while(w.hasNext() && !foundEnd) {
			char next = w.next();
			switch(next) {
			case '"':
				foundEnd = true;
				break;
				default:
					readString += next;
			}
		}
		
		if(!foundEnd) 
			throw new IllegalArgumentException(illegalArg + readRest(w));
		return readString;
	}
	
	private static Object parseArray(JSONWrapper w) {
		List<Object> readObject = new ArrayList<>();
		boolean readArray = false;
		while(w.hasNext() && !readArray) {
			char current = w.next();
			switch (current) {
			case ' ':
			case ',': 
				break;
			case '"':
				String value = parseString(w);
				readObject.add(value);
				break;
			case '{':
				JSONString jString = parseJsonObject(w);
				readObject.add(jString);
				break;
			case '[':
				Object otherArray = parseArray(w);
				readObject.add(otherArray);
				break;
			case ']':
				readArray = true;
				break;
			default:
				try {
					String number = readNumber(w, current);
					
					if(number != "") {
						if(number.contains(".")) {
							Double d = Double.parseDouble(number);
							readObject.add(d);
						}else {
							Integer i = Integer.parseInt(number);
							readObject.add(i);
						}
					}
					break;
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalArgumentException(illegalArg + " " + current + readRest(w));
				}
			}
		}
		
		return readObject.toArray();
	}
	
	private static String readRest(JSONWrapper w) {
		String rest = "";
		while(w.hasNext()) {
			rest += w.next();
		}
		return rest;
	}
	
	private static char[] numberChars = new char[] {'+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private static boolean isNumberCharacter(char c) {
		boolean isNumber = false;
		for(char ch : numberChars) {
			if(ch == c) isNumber = true;
		}
		return isNumber;
	}
	
	private static String readNumber(JSONWrapper w, char first) {
		if(!isNumberCharacter(first)) {
			return "";
		}
		
		String number = ""+first;
		boolean doneReading = false;
		while(w.hasNext() && !doneReading) {
			char current = w.next();
			switch (current) {
			case ' ':
				break;
			case ',':
			case ']':
			case '}':
				doneReading = true;
				break;

			case '"':
			default:
				if((current >= '0' && current <= '9') || current == '.' || current == ',' || current == '-' || current == '+') {
					number += current;
					break;
				}
				throw new IllegalArgumentException(illegalArg + " " + current + readRest(w));
			}
		}
		return number;
	}
	
	public static String encode(String key, Object o) {
		if(o instanceof String) {
			return "\"" + key + "\" : \"" + ((String)o) + "\"";
		}else if(o instanceof Integer) {
			return "\"" + key + "\" : " + ((Integer)o);
		}else if(o instanceof Double) {
			return "\"" + key + "\" : " + ((Double)o);
		} else if(o instanceof String[]) {
			String out = "\"" + key + "\" : [\"%s\"]";
			String[] values = (String[])o;
			int count = 0;
			for(String v : values) {
				out = String.format(out, v + (count++ < values.length-1 ? "\", \"%s" : ""));
			}
			return out;
		}else if(o instanceof JSONString) {
			return "\"" + key + "\" : " + ((JSONString)o).encode();
		}else if(o instanceof Object[]){
			String out = "\"" + key + "\" : [%s]";
			Object[] values = (Object[])o;
			int count = 0;
			for(Object v : values) {
				out = String.format(out, encode(v) + (count++ < values.length-1 ? ", %s" : ""));
			}
			return out;
		}
		return "";
	}
	
	public static String encode(Object o) {
		if(o instanceof String) {
			return "\"" + ((String)o) + "\"";
		}else if(o instanceof Integer) {
			return ((Integer)o)+"";
		}else if(o instanceof Double) {
			return ((Double)o)+"";
		}else if(o instanceof String[]) {
			String out = "[\"%s\"]";
			String[] values = (String[])o;
			int count = 0;
			for(String v : values) {
				out = String.format(out, v + (count++ < values.length-1 ? "\",\"%s" : ""));
			}
			return out;
		}else if(o instanceof JSONString) {
			return ((JSONString)o).encode();
		}else if(o instanceof Object[]){
			String out = "[\"%s\"]";
			Object[] values = (Object[])o;
			int count = 0;
			for(Object v : values) {
				out = String.format(out, encode(v) + (count++ < values.length-1 ? "\",\"%s" : ""));
			}
			return out;
		}
		return "";
	}
	
	public String encode() throws IllegalArgumentException{
		String out = "{ %s }";
		int count = 0;
		for(String key : valueMap.keySet()) {
			out = String.format(out, JSONString.encode(key, valueMap.get(key)) + (count++ < valueMap.keySet().size()-1 ? ", %s" : ""));
		}
		return out;
	}
	
	@Override
	public String toString() {
		String output = "{ }";
		try {
			output = this.encode();
		}catch(Exception e) {
			return output;
		}
		return output;
	}
	
}

class JSONWrapper{
	private int current = 0;
	private char[] jstring;
	
	public JSONWrapper(String s) {
		this.jstring = s.toCharArray();
	}
	
	public boolean hasNext() {
		return current < jstring.length;
	}
	
	public char next() {
		return jstring[current++];
	}
	
	public char current() {
		return jstring[current];
	}
}
