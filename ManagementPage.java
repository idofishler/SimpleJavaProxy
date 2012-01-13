import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class ManagementPage {


	private static final String LINK_TO_MGMT_PAGE = "linky here";
	/*
	 * HTML code templates for returned pages.
	 */
	private static final String MGMT_PAGE ="<html>\n<body>\n" +
	"<h1>Welcome to the Management Page!</h1>\n<h2>B" +
	"locked Resource:</h2>\n%s\n<h2>Blocked Pages:<" +
	"/h2>\n%s\n<form method=\"post\"><input type=\"" +
	"text\" name=\"NewRule\"><input type=\"submit\" " +
	"name=\"Add Rule\"></form></body>\n</html>"; 

	private static final String RULE_ADDED_SUCCESSFULLY_FORMAT =
		"<html>\n<body>\n<h1>Policy {1} successfully added!</" +
		"h1>\nClick <a href=\"" + LINK_TO_MGMT_PAGE + "\">her" +
		"e</a> to return.\n</body>\n</html>";
	private static final String RULE_ADDED_FAILED_FORMAT_FORMAT =
		"<html>\n<body>\n<h1>Policy {1} successfully added!" +
		"</h1>\nClick <a href=\"" + LINK_TO_MGMT_PAGE +"\">" +
		"here</a> to return.\n</body>\n</html>";
	private static final String RULE_DELETED_SUCCESSFULLY_FORMAT =
		"<html>\n<body>\n<h1>Policy {1} successfully remove" +
		"d!</h1>\nClick <a href=\"" + LINK_TO_MGMT_PAGE +
		"\">here</a> to return.\n</body>\n</html>";

	private static final String FAILED = "Failed output stream";
	private static final String BLOCK_SITE = "block-site";
	private static final String BLOCK_RESOURCE = "block-resource";


	private PolicyFile m_policy;
	private Socket m_socket;

	public ManagementPage(Socket newSocket, PolicyFile newPolicy)
	{
		m_policy = newPolicy;
		m_socket = newSocket;
	}

	/**
	 * 
	 * @param i_newPolicyName
	 * @return if policy name is malformed (isn't a proper policy)
	 */
	protected boolean isMalformedPolicyName(String i_newPolicyName)
	{
		boolean malformed = false;
		if (i_newPolicyName == null)
		{
			malformed = true;
		}
		else
		{
			if (!i_newPolicyName.equals(BLOCK_SITE) && !i_newPolicyName.equals(BLOCK_RESOURCE))
			{
				malformed = true;
			}
		}
		return malformed;
	}


	/*
	 * Display management page
	 */
	public void go()
	{
		StringBuilder blockedResources = new StringBuilder();
		StringBuilder blockedHosts = new StringBuilder();
		String outputPage;
		ArrayList<String> resourceList = m_policy.getBlockedResorces();
		ArrayList<String> hostsList = m_policy.getBlockedHosts();
		PrintWriter output;

		try {
			output = new PrintWriter(m_socket.getOutputStream(),true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		/*
		 * Generate list of blocked items
		 */
		blockedResources.append("<form method=\"post\">");
		for (String resourse : resourceList)
		{
			blockedResources.append(resourse +
					"\t<input type=\"submit\" name=\"" + BLOCK_RESOURCE + " " + resourse +
			"\" value=\"Delete Rule\"/><br>");
		}
		for (String host : hostsList)
		{
			blockedHosts.append(host +
					"\t<input type=\"submit\" name=\"" + BLOCK_SITE + " " + host +
			"\" value=\"Delete Rule\"/><br>");
		}
		blockedHosts.append("</form>");


		outputPage = String.format(MGMT_PAGE, blockedResources.toString(), blockedHosts.toString());

		output.println(outputPage);

		output.flush();
		output.close();

		//FOR DEBUGGING
		System.out.println(outputPage); 
	}
}
