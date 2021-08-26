package com.server.web.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.logger.Level;
import com.server.main.Server;
import com.sun.net.httpserver.HttpHandler;

public class WebFileManager {

	private static Map<String, Path> files = new HashMap<>();
	
	public static void addFile(String name, Path path) {
		files.put(name, path);
	}
	
	public static HttpHandler getDefaultFileHandler(String fileName) {
		return (he)->{
			Server.getLogger().log(Level.INFO, "Request for: " + fileName);
			if(files.get(fileName) != null) {
				try {
					byte[] output = Files.readAllBytes(files.get(fileName));
					he.sendResponseHeaders(200, output.length);
					he.getResponseBody().write(output);
					he.getResponseBody().flush();
					he.getResponseBody().close();
					return;
				} catch (Exception e) {
					Server.getLogger().log(Level.ERROR, "Could not get file to send: " + fileName + " from: " + files.get(fileName).toString());
				}
			}
			String responce = "Page not found";
			he.sendResponseHeaders(404, responce.length());
			he.getResponseBody().write(responce.getBytes());
			he.getResponseBody().flush();
			he.getResponseBody().close();
		};
	}
	
}
