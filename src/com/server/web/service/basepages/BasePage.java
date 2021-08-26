package com.server.web.service.basepages;

public class BasePage {

	private static String basePageHead = "<head>"
			+ "<title>WebEditor</title>"
			+ "<link rel=\"stylesheet\" href=\"css/mainPage.css\">"
			+ "<link rel=\"stylesheet\" href=\"css/myTable.css\">"
			+ "<link rel=\"stylesheet\" href=\"css/customScroll.css\">"
			+ "<link rel=\"stylesheet\" href=\"css/customFonts.css\">"
			+ "<link rel=\"icon\" href=\"assets/servericon.png\">"
			+ "<script src=\"js/fillTable.js\"></script>"
			+ "</head>";
	
	private static String basePageBody = "<body>\r\n"
			+ "        <div class=\"grid\">\r\n"
			+ "            <div class=\"sideContent\">\r\n"
			+ "                    <div class=\"TopSelection\">\r\n"
			+ "{SERVER}"
			+ "                    </div>\r\n"
			+ "                <div class=\"sideMenu\" id=\"sideMenu1\">\r\n"
			+ "                    <div class=\"sideMenuItem\">\r\n"
			+ "                        <input type=\"radio\" name=\"SideSelection\">\r\n"
			+ "                        <span>Connection_01</span>\r\n"
			+ "                    </div>\r\n"
			+ "                </div>\r\n"
			+ "            </div>\r\n"
			+ "            <div class=\"pageBody\">\r\n"
			+ "                <div class=\"TobMenuBody\" id=\"topmenu1\">\r\n"
			+ "                    <button onclick=\"testRequest();\">API CALL</button>\r\n"
			+ "                </div>\r\n"
			+ "                <div class=\"pageBodyItem\">\r\n"
			+ "                    <div class=\"DataTable\">\r\n"
			+ "                        <div class=\"DataHeaders\" id=\"table1_head\">\r\n"
			+ "                            <div class=\"DataHeader_Container\">\r\n"
			+ "                                <span class=\"DataHeader_item\">test</span>\r\n"
			+ "                            </div>\r\n"
			+ "                        </div>\r\n"
			+ "                        <div class=\"DataBody\" Id=\"table1\">\r\n"
			+ "                            <div class='DataItems'>\r\n"
			+ "                                <span class='DataItem'>item1</span>\r\n"
			+ "                            </div>\r\n"
			+ "                        </div>\r\n"
			+ "                    </div>\r\n"
			+ "                </div>\r\n"
			+ "            </div>\r\n"
			+ "        </div>\r\n"
			+ "    </body>";
	
	private static String serverButton = 
			"<div class=\"TopSelectionItem\" onclick=\"fillSideTable('shops');\" id=\"shopsSelection\">"
			+ "<img src=\"assets/servericon2.png\">"
			+ "<span>{SERVER_NAME}</span></div>";
	
	public static String getPageContent() {
		return "<html>" + basePageHead + basePageBody.replace("{SERVER}", serverButton) + "</html>";
	}
	
}
