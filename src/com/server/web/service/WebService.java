package com.server.web.service;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.logger.Level;
import com.server.main.Server;
import com.sun.net.httpserver.HttpServer;

public class WebService {
	
	private int port;
	
	private HttpServer webServer;
		
	public WebService(int port) {		
		this.port = port;
		try {
			webServer = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			Server.logger.log(Level.ERROR, e.getMessage());
		}
		if(webServer != null) {
			webServer.start();
			init();
		}
		Server.getLogger().log(Level.INFO, "Web server running on: localhost:" + port);
	}
	
	public void registerPage(String path, String fileName, Path filePath) {
		WebFileManager.addFile(fileName,filePath);
		webServer.createContext(path, WebFileManager.getDefaultFileHandler(fileName));
	}
	
	private void init() {
		String path = Paths.get("res","WebTool").toString();
		registerPage("/", "index.html", Paths.get(path, "index.html"));
		registerPage("/css/mainPage.css", "mainPage.css", Paths.get(path, "css", "mainPage.css"));
		registerPage("/css/myTable.css", "myTable.css", Paths.get(path, "css", "myTable.css"));
		registerPage("/css/customFonts.css", "customFonts.css", Paths.get(path, "css", "customFonts.css"));
		registerPage("/css/customScroll.css", "customScroll.css", Paths.get(path, "css", "customScroll.css"));
		registerPage("/js/fillTable.js", "fillTable.js", Paths.get(path, "js", "fillTable.js"));
		registerPage("/assets/servericon2.png", "servericon2.png", Paths.get(path, "assets", "servericon2.png"));
		registerPage("/assets/servericon.png", "servericon.png", Paths.get(path, "assets", "servericon.png"));
	
	}
	
	public static void main(String... args) {
		
		WebService s = new WebService(25565);
		
	}
	
}
