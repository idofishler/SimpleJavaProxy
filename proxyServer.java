
public class proxyServer {

	private static Logger errorLogger = new Logger(System.err);
	private static int m_port;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		if (args.length != 2) {
//			errorLogger.log("Usege: proxyServer [port] [policy file]");
//			System.exit(1);
//		}
		try {
			m_port = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException nfe) {
			errorLogger.log("Invalid port number");
		}
		
		//String policyFileName = args[1];
		// TODO: open the policy file and read it...
		
		Thread mainReciverThread = new Thread(new ProxyReciver(m_port));
		mainReciverThread.start();
		try {
			mainReciverThread.join();
		} catch (InterruptedException e) {
			errorLogger.log(e);
		}
	}

}
