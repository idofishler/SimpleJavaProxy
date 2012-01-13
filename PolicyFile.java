/**
 * 
 * This class is a represent a policy file in terms of this Lab only
 * 
 * There are two possible policy rules:
 * 1. block-site “[keyword]” – block request if the host contains the keyword.
 * 2. block-resource “[keyword]” – block request if the requested resource contains the keyword.
 * 
 * The policy file format should look as follows:
 * [policy rule 1][CRLF]
 * [policy rule 2][CRLF]
 * [policy rule 3][CRLF]
 * .
 * .
 * .
 * 
 * Example of a policy rule:
 * block-site “google.com/plus”
 * block-site “google.com/mail”
 * block-resource “.avi”
 * block-site “facebook.com”
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * @author Ido Fishler, Dor Tumarkin.
 * <BR><BR>
 * This class is a represent a policy file in terms of this Lab only<BR>
 * <BR>
 * There are two possible policy rules:<BR>
 * 1. block-site “[keyword]” – block request if the host contains the keyword.<BR>
 * 2. block-resource “[keyword]” – block request if the requested resource contains the keyword.<BR>
 * <BR>
 * The policy file format should look as follows:<BR>
 * [policy rule 1][CRLF]<BR>
 * [policy rule 2][CRLF]<BR>
 * [policy rule 3][CRLF]<BR>
 * .<BR>
 * .<BR>
 * .<BR>
 * 
 * Example of a policy rule:<BR>
 * block-site “google.com/plus”<BR>
 * block-site “google.com/mail”<BR>
 * block-resource “.avi”<BR>
 * block-site “facebook.com”<BR>
 * 
 */
public class PolicyFile {

	public static final String BLOCK_SITE = "block-site";
	public static final String BLOCK_RESOURCE = "block-resource";
	
	private static Logger m_logger = new Logger();
	private File policyFile;
	private ArrayList<String> blockedHosts;
	private ArrayList<String> blockedResorces;

	/**
	 * @param fileName - pull path to policy file
	 * @throws FileNotFoundException 
	 */
	public PolicyFile(String fileName) throws FileNotFoundException {
		policyFile = new File(fileName);
		blockedHosts = new ArrayList<String>();
		blockedResorces = new ArrayList<String>();
		parse();
	}

	/**
	 * @return all the blocked hosts
	 */
	public ArrayList<String> getBlockedHosts() {
		return blockedHosts;
	}

	/**
	 * @return all the blocked resources
	 */
	public ArrayList<String> getBlockedResorces() {
		return blockedResorces;
	}

	/**
	 * This will add a rule to the appropriate list (blocked-hosts or blocked-resources)
	 * @param rule a valid rule(after syntax validation!!!)
	 * @throws IOException if policy file is not writable
	 */
	public void addRule(String rule) throws IOException {
		fillLists(rule);
		writeRulesToFile();
	}
	
	/**
	 * This will remove the rule from the appropriate list.
	 * If the rule dosn't exist will do nothing
	 * @param ruleToRemove the rule to be removed (the whole line)
	 * @return status - 0 is ok, -1 is error - could not find the rule to remove
	 * @throws IOException if policy file is not writable.
	 */
	public int removeRule(String ruleToRemove) throws IOException {
		int status = 0;
		String[] attributs = new String[2];
		attributs = ruleToRemove.split(" ");
		String ruleType = attributs[0];
		String ruleValue = attributs[1].replaceAll("\"", "");
		int pos;
		if (BLOCK_SITE.equalsIgnoreCase(ruleType)) {
			pos = findRulePos(blockedHosts, ruleValue);
			if (pos != -1) {
				blockedHosts.remove(pos);				
			} else {
				status = -1;
			}
		} else if (BLOCK_RESOURCE.equalsIgnoreCase(ruleType)) {     
			pos = findRulePos(blockedResorces, ruleValue);
			if (pos != -1) {
				blockedResorces.remove(pos);				
			} else {
				status = -1;
			}
		}
		writeRulesToFile();
		return status;
	}
	

	/**
	 * Load the object's fields
	 * @throws FileNotFoundException
	 */
	private void parse() throws FileNotFoundException {
	
		try {
			
			BufferedReader input =  new BufferedReader(new FileReader(policyFile));
			
			try {
				String line = null;
				while ((line = input.readLine()) != null){
					fillLists(line);
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			m_logger.log(ex, "Problem with parsing policy file");
		}
	}

	private void fillLists(String line) {
		String[] attributs = new String[2];
		attributs = line.split(" ");
		if (BLOCK_SITE.equalsIgnoreCase(attributs[0])) {
			blockedHosts.add((attributs[1].replaceAll("\"", "")));
		} else if (BLOCK_RESOURCE.equalsIgnoreCase(attributs[0])) {     
			blockedResorces.add((attributs[1].replaceAll("\"", "")));
		}
	}

	private void writeRulesToFile() throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(policyFile));
		for (String rule : blockedHosts) {
			out.write(BLOCK_SITE + " \"" + rule + "\"\n");
		}
		for (String rule : blockedResorces) {
			out.write(BLOCK_RESOURCE + " \"" + rule + "\"\n");
		}
	}

	private int findRulePos(ArrayList<String> rules, String value) {
		int pos = -1;
		for (int index = 0; index < blockedHosts.size(); index++) {
			if (blockedHosts.get(index).equalsIgnoreCase(value)) {
				pos = index;
			}
		}
		return pos;
	}
}
