import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ProxyReciver {

	private int m_port;
	private PolicyFile m_policyFile;

	private static Logger m_logger = new Logger();
	private static Logger m_errorLogger = new Logger(System.err);

	public ProxyReciver(int port, PolicyFile policyFile) {
		m_port = port;
		m_policyFile = policyFile;
	}

	public void listen() {
		
		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		try {
			welcomeSocket = new ServerSocket(m_port);
			m_logger.log("Listening on Socket: " + m_port + "\n");
		} catch (IOException ioe) {
			m_errorLogger.log(ioe, "Problem with welcome socket");
		}
		while (true) {
			try {
				connectionSocket = welcomeSocket.accept();
			} catch (IOException e) {
				m_errorLogger.log(e, "Problem with connetction socket: " + connectionSocket.toString());
			}
			// the request handler will close the clientSocket when finish
			RequestHandler requestHandler = new RequestHandler(connectionSocket, m_policyFile);
			Thread thread = new Thread(requestHandler);
			thread.start();
			m_logger.log("Thread: " + thread.getName() + " started");
		}
	}
}
