package com.logger;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

public class Logger {

	private Printer p; 
	
	private PrintMode mode;
	
	private PrintingType type;
		
	public Logger(PrintingType printingType, PrintMode mode) {
		this.type = printingType;
		this.mode = mode;
		FileAccess fileAccess = null;
		if(printingType == PrintingType.FlatFile || printingType == PrintingType.FileAndConsole) fileAccess = new FileAccess("Log");
		p = new Printer(printingType, mode, fileAccess);
	}
	
	public Logger(String path, PrintMode mode) {
		this.type = PrintingType.FlatFile;
		this.mode = mode;
		FileAccess fileAccess = new FileAccess(path);
		p = new Printer(PrintingType.FlatFile, mode, fileAccess);
	}
	
	public void log(Level level, String s) {
		String out = getStackTrace(level);
		if(s == null) {
			out += " [string was null]";
		}else {
			out += " " + s;
		}
		p.print(level, out);
	}
	
	public void log(Level level, String s, Class<?> type) {
		String out = getStackTrace(level);
		
		out += (type != null ? " " + type.getName() + " " : " ");		
		out += (s != null ? s : "[Exception was null]");

		p.print(level, out);
	}
	
	public void log(Level level, Exception e, Class<?> type) {
		String out = getStackTrace(level);
		
		out += (type != null ? " " + type.getName() + " " : " ");		
		out += (e != null ? e.getMessage() : "[Exception was null]");

		p.print(level, out);
	}
	
	public void log(Level level, Exception e) {
		String out = getStackTrace(level);
		
		out += " " + (e != null ? e.getMessage() : "[Exception was null]");

		p.print(level, out);
	}
	
	public void log(Level level, Collection<?> l) {
		String out = getStackTrace(level);
		
		if(l != null) {
			out += " collection:- Size(" + l.size() + ")->{";
			
			if(l.size() > 0) {
				out += l.stream().map(x -> x.toString()).reduce("", (x, y) -> x + "," + y)+ "}";
			}else {
				out += "-}";
			}
		}else {
			out += " [collection was null]";
		}

		p.print(level, out);
	}
	
	public void log(Level level, Map<?, ?> m) {
		String out = getStackTrace(level);
		
		if(m != null) {
			if(m.isEmpty()) {
				out += " [map was empity]";
			}else {
				out += " map:- Size(" + m.size() + ")->{";
				out += m.entrySet().stream().map(x -> {
					Object k = x.getKey();
					String kstring = "";
					Object v = x.getValue();
					String vstring = "";
					kstring = (k == null ? "nullObject" : k.toString());
					vstring = (v == null ? "nullObject" : v.toString());
					return "{" + kstring + ", " + vstring + "}";
				}).reduce("", (x,y) -> x + "," + y) + "}";
			}
		}else {
			out += " [map was null]";
		}

		p.print(level, out);
	}
		
	public void log(Level level, Object o) {
		String out = getStackTrace(level);
		
		if(o != null) {
			out += " Object:" + o.getClass().getName() + "\n";
			Field[] f = o.getClass().getDeclaredFields();
			
			for(int i = 0; i < f.length; i++) {
				boolean accasibility = f[i].canAccess(o);
				f[i].setAccessible(true);
				
				Object value = null;
				try {
					value = f[i].get(o);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
				f[i].setAccessible(accasibility);
				
				out += "\tFieldType: " + Modifier.toString(f[i].getModifiers()) + " "
				+ f[i].getType().toString() 
				+ "-> " + f[i].getName() 
				+ ": " + (value == null ? "null" : value.toString()) + (i < f.length-1 ? "\n" : "");
				
			}
		}else {
			out += " [object was null]";
		}

		p.print(level, out);
	}
	
	public void log(Level level, LogInterface i) {
		String out = getStackTrace(level);
		
		out += " " + i.log();

		p.print(level, out);
	}
	
	private String getStackTrace(Level level) {
		if(this.mode == PrintMode.DebugNoTrace) return "";
		if(this.mode == PrintMode.Event && level != Level.DEBUG) return "";
		StackTraceElement e = new Exception().getStackTrace()[2];
		return "Trace:" + e.getClassName() + "." + e.getMethodName() + "() Line:" + e.getLineNumber();
	}
	
	public void redirectOutput(OutputStream out) throws IllegalAccessException {
		if(this.type != PrintingType.Console) throw new IllegalAccessException("Can not set out put stream for flat file storage");
		this.p.setOutputStream(out);
	}

	public void setMode(PrintMode mode) {
		this.mode = mode;
		this.p.setMode(mode);
	}
}
