import java.io.PrintStream;

/**
 * 
 * @author Ido Fishler
 *
 * This is the main logger of this project
 */
public class Logger {
	
	private PrintStream logTo;
	
	/**
	 * Default constructor - will log to System.out
	 */
	public Logger() {
		logTo = System.out;
	}
	
	/**
	 * 
	 * @param logTo
	 */
	public Logger(PrintStream logTo) {
		this.logTo = logTo;
	}
	
	/**
	 * 
	 * @param message
	 */
	public void log(String message) {
		logTo.println(message);
	}
	
	/**
	 * 
	 * @param exception
	 */
	public void log(Exception exception) {
		logTo.println("Stack trace:");
		exception.printStackTrace(logTo);
		logTo.println("-----------------------------------------------");
		logTo.println("Exception Meggage:\n" + exception.getMessage());
	}
	
	/**
	 * 
	 * @param exception
	 * @param message
	 */
	public void log(Exception exception, String message) {
		logTo.println("Additional information:\n" + message);
		logTo.println("-----------------------------------------------");
		log(exception);
	}
}
