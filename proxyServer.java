import java.io.FileNotFoundException;


public class proxyServer {

	public static final String CRLF = "\r\n";
	
	private static Logger errorLogger = new Logger(System.err);
	private static int m_port;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 2) {
			errorLogger.log("Usege: proxyServer [port] [policy file]");
			System.exit(1);
		}
		PolicyFile policyFile = null;
		try {
			m_port = Integer.parseInt(args[0]);
			String policyFileName = args[1];
			policyFile = new PolicyFile(policyFileName);
		}
		catch (NumberFormatException nfe) {
			errorLogger.log("Invalid port number");
		} catch (FileNotFoundException fnfe) {
			errorLogger.log("Can't find poicy file at: " + args[1]);
		}
		

		ProxyReciver listener = new ProxyReciver(m_port, policyFile);
		listener.listen();
	}

}
