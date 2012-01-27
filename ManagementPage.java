import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;


public class ManagementPage {

	private static final String BLOCK_SITE = "block-site";
	private static final String BLOCK_RESOURCE = "block-resource";
	private static final String DELETE_RULE = "Delete Rule";
	private static final String ADD_RULE = "Add Rule";
	private static final String NEW_RULE = "NewRule";
	private static final String SPACE_SEPARETOR = " ";
	private static final String EQUAL_SEPERATOR = "=";
	private static final String LINK_TO_MGMT_PAGE = "http://content-proxy/management";

	/*
	 * HTML code templates for returned pages.
	 */
	private static final String MGMT_PAGE ="<html>\n<body>\n" +
	"<h1>Welcome to the Management Page!</h1>\n<h2>B" +
	"locked Resource:</h2>\n%s\n<h2>Blocked Pages:<" +
	"/h2>\n%s\n<form method=\"post\"><input type=\"" +
	"text\" name=\"NewRule\"><input type=\"submit\" " +
	"value=\"" + ADD_RULE + "\"></form></body>\n</html>"; 

	private static final String RULE_ADDED_SUCCESSFULLY_FORMAT =
		"<html>\n<body>\n<h1>Policy [%s] successfully added!</" +
		"h1>\n<a href=\"" + LINK_TO_MGMT_PAGE + "\">Back" +
		"</a>\n</body>\n</html>";
	private static final String RULE_ADDED_FAILED_FORMAT_FORMAT =
		"<html>\n<body>\n<h1>Error while trying to add rule [%s]" +
		"</h1>\n<a href=\"" + LINK_TO_MGMT_PAGE +"\">" +
		"back</a>\n</body>\n</html>";
	private static final String RULE_DELETED_SUCCESSFULLY_FORMAT =
		"<html>\n<body>\n<h1>Policy [%s] successfully removed" +
		"!</h1>\n<a href=\"" + LINK_TO_MGMT_PAGE +
		"\">Back</a>\n</body>\n</html>";
	private static final String RULE_DELETED_FAILED_FORMAT =
		"<html>\n<body>\n<h1>Error while trying to remove rule [%s]" +
		"d!</h1>\n<a href=\"" + LINK_TO_MGMT_PAGE +
		"\">Back</a>\n</body>\n</html>";

	private static Logger m_Logger = new Logger();
	private PolicyFile m_policy;
	private Socket m_socket;
	private PrintWriter m_output;
	

	public ManagementPage(Socket newSocket, PolicyFile newPolicy)
	{
		m_policy = newPolicy;
		m_socket = newSocket;
	}

	/**
	 * 
	 * @param ruleType
	 * @param ruleValue 
	 * @return if policy name is malformed (isn't a proper policy)
	 */
	private boolean isMalformedPolicyName(String ruleType, String ruleValue)
	{
		boolean malformed = false;
		if (ruleType == null || ruleValue == null)
		{
			malformed = true;
		}
		else
		{
			if ((!ruleType.equals(BLOCK_SITE) && !ruleType.equals(BLOCK_RESOURCE)) ||
					!(ruleValue.startsWith("\"") && ruleValue.endsWith("\""))) {
					malformed = true;					
			}
		}
		return malformed;
	}


	/**
	 * Entry point for management page
	 * @param query 
	 */
	public void go(String query)
	{
		try {
			m_output = new PrintWriter(m_socket.getOutputStream(),true);
			
			if (query == null) {
				doInitPage();
			} else {
				proccesRequst(query);
			}
			
		} catch (IOException e) {
			m_Logger.log(e);
		}	
	}

	private void proccesRequst(String query) {
		String html;
		try {
			query = URLDecoder.decode(query, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			m_Logger.log(e1);
		}
		
		if (query.indexOf(NEW_RULE) > -1) {
			html = proccesNewRule(query);
		} else {
			html = proccesRemoveRule(query);
		}
		m_output.println(html);
	}

	private String proccesRemoveRule(String query) {
		String html = "";
		String[] args = query.split(EQUAL_SEPERATOR);
		String[] rule = args[0].split(SPACE_SEPARETOR);
		
		String ruleType = rule[0];
		String ruleValue = "\"" + rule[1] + "\"";
		String action = args[1];
		
		if (DELETE_RULE.equalsIgnoreCase(action)) {
			try {
				int status = m_policy.removeRule(args[0]);
				if (status == 0) {
					html = String.format(RULE_DELETED_SUCCESSFULLY_FORMAT, 
							ruleType + SPACE_SEPARETOR + ruleValue);
				} else {
					html = String.format(RULE_DELETED_FAILED_FORMAT,
							ruleType + SPACE_SEPARETOR + ruleValue);
				}
			} catch (IOException e) {
				html = String.format(RULE_DELETED_FAILED_FORMAT,
						ruleType + SPACE_SEPARETOR + ruleValue);
			}
		}
		return html;
	}

	private String proccesNewRule(String query) {
		String html = "";
		String[] args = query.split(EQUAL_SEPERATOR);
		String[] rule = args[1].split(SPACE_SEPARETOR);
		
		html = String.format(RULE_ADDED_FAILED_FORMAT_FORMAT, args[1]); 
		
		if (rule.length != 2) {
			return html; 
		}
		String ruleType = rule[0];
		String ruleValue = rule[1];
		
		if (isMalformedPolicyName(ruleType, ruleValue)) {
			return html;
		} else {
			try {
				m_policy.addRule(args[1]);
				html = String.format(RULE_ADDED_SUCCESSFULLY_FORMAT, args[1]);
			} catch (IOException e) {
				return html;
			}
		}
		return html;
	}

	/**
	 * Generate list of blocked items
	 */
	private void doInitPage() {
		ArrayList<String> resourceList = m_policy.getBlockedResorces();
		ArrayList<String> hostsList = m_policy.getBlockedHosts();
		StringBuilder blockedResources = new StringBuilder();
		StringBuilder blockedHosts = new StringBuilder();
		String outputPage;
		
		blockedResources.append("<form method=\"post\">");
		for (String resourse : resourceList)
		{
			blockedResources.append(resourse +
					"\t<input type=\"submit\" name=\"" + BLOCK_RESOURCE + " " + resourse +
			"\" value=\"" + DELETE_RULE + "\"/><br>");
		}
		for (String host : hostsList)
		{
			blockedHosts.append(host +
					"\t<input type=\"submit\" name=\"" + BLOCK_SITE + " " + host +
			"\" value=\"" + DELETE_RULE + "\"/><br>");
		}
		blockedHosts.append("</form>");

		outputPage = String.format(MGMT_PAGE, blockedResources.toString(), blockedHosts.toString());

		m_output.println(outputPage);
		m_output.flush();
		m_output.close();
	}
}
