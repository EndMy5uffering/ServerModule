package com.server.web.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.logger.Level;
import com.server.main.Server;
import com.server.web.service.basepages.BasePage;
import com.sun.net.httpserver.HttpServer;

public class WebService {
	
	private int port;
	
	private HttpServer webServer;
	private HashMap<Integer, Server> portsToServer = new HashMap<>();
		
	public WebService(int port) {
		this.port = port;
		try {
			webServer = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			Server.logger.log(Level.INFO, e.getMessage());
		}
		if(webServer != null) {
			webServer.start();
			init();
		}
		Server.getLogger().log(Level.INFO, "Web server running on: localhost:" + port);
	}
	
	public void registerServer(int port, Server s) {
		this.portsToServer.put(port, s);
	}
	
	public void registerPage(String path, String fileName, Path filePath) {
		WebFileManager.addFile(fileName,filePath);
		webServer.createContext(path, WebFileManager.getDefaultFileHandler(fileName));
	}
	
	private void init() {
		URL url = getClass().getResource("../../../..");
		Path path;
		try {
			path = Paths.get(url.toURI()).getParent().resolve("res").resolve("WebTool");
			//registerPage("/", "index.html", path.resolve("index.html"));
			registerPage("/css/mainPage.css", "mainPage.css", path.resolve(Paths.get("css", "mainPage.css")));
			registerPage("/css/myTable.css", "myTable.css", path.resolve(Paths.get("css", "myTable.css")));
			registerPage("/css/customFonts.css", "customFonts.css", path.resolve(Paths.get("css", "customFonts.css")));
			registerPage("/css/customScroll.css", "customScroll.css", path.resolve(Paths.get("css", "customScroll.css")));
			registerPage("/js/fillTable.js", "fillTable.js", path.resolve(Paths.get("js", "fillTable.js")));
			registerPage("/assets/servericon2.png", "servericon2.png", path.resolve(Paths.get("assets", "servericon2.png")));
			registerPage("/assets/servericon.png", "servericon.png", path.resolve(Paths.get("assets", "servericon.png")));
		
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		webServer.createContext("/", (he) -> {
			String page = BasePage.getPageContent();
			he.sendResponseHeaders(200, page.length());
			he.getResponseBody().write(page.getBytes());
			he.getResponseBody().flush();
			he.getResponseBody().close();
		});
		
		webServer.createContext("/api/test", (he) -> {
			System.out.println(he.getRequestURI().toString());
			System.out.println(he.getRequestMethod());
			System.out.println(he.getRequestURI().getRawPath());
			System.out.println(he.getRequestURI().getRawQuery());
			InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            br.lines().forEach(x -> {System.out.println(x);});
            
			String response = "{\"message\": \"response\"}";
			he.sendResponseHeaders(200, response.length());
			he.getResponseBody().write(response.getBytes());
			he.getResponseBody().flush();
			he.getResponseBody().close();
		});
		
	}
	
	public static void main(String... args) {
		
		WebService s = new WebService(25565);
				
	}
	
}
