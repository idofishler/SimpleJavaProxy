import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class PolicyFile {

	private static final String BLOCK_SITE = "block-site";
	private static final String BLOCK_RESOURCE = "block-resource";
	
	private static Logger m_logger = new Logger();
	private File policyFile;
	private ArrayList<String> blockedHosts;
	private ArrayList<String> blockedResorces;

	public PolicyFile(String fileName) {
		policyFile = new File(fileName);
	}

	public void parse() throws FileNotFoundException {

		blockedHosts = new ArrayList<String>();
		blockedResorces = new ArrayList<String>();

		try {
			
			BufferedReader input =  new BufferedReader(new FileReader(policyFile));
			
			try {
				String line = null;
				while ((line = input.readLine()) != null){
					String[] attributs = new String[2];
					attributs = line.split(" ");
					if (BLOCK_SITE.equalsIgnoreCase(attributs[0])) {
						blockedHosts.add(attributs[1]);
					} else if (BLOCK_RESOURCE.equalsIgnoreCase(attributs[0])) {
						// TODO replace this       
						blockedResorces.add(attributs[1].replaceAll("\"", ""));
					}
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
	
	public ArrayList<String> getBlockedHosts() {
		return blockedHosts;
	}

	public ArrayList<String> getBlockedResorces() {
		return blockedResorces;
	}

	public void addRule(String rule) {
		// TODO implement this later
	}
	
	public void removeRule(String rule) {
		// TODO implement
	}
	
}
