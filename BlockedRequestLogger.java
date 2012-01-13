
public class BlockedRequestLogger {

	private static BlockedRequestLogger instance = null;
	
	private StringBuilder log;
	
	private BlockedRequestLogger() {
		log = new StringBuilder();
	}

	public static BlockedRequestLogger getInstance() {
		if(instance == null) {
			instance = new BlockedRequestLogger();
		}
		return instance;
	}
	
	public synchronized void log(String blockRequest) {
		log.append(blockRequest);
	}
	
	public String getLog() {
		return log.toString();
	}
}
