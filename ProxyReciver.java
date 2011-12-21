import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class ProxyReciver implements Runnable {

	private int m_port;
	private static Logger m_logger = new Logger();

	public ProxyReciver(int port) {
		m_port = port;
	}

	@Override
	public void run() {
		
		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		try {
			welcomeSocket = new ServerSocket(m_port);
		} catch (IOException ioe) {
			m_logger.log(ioe, "Problem with welcome socket");
		}
		while (true) {
			try {
				connectionSocket = welcomeSocket.accept();
			} catch (IOException e) {
				m_logger.log(e, "Problem with connetction socket: " + connectionSocket.toString());
			}
			// the request handler will close the clientSocket when finish
			RequestHandler requestHandler = new RequestHandler(connectionSocket);
			Thread thread = new Thread(requestHandler);
			thread.start();
			m_logger.log("Thread: " + thread.getName() + " started");
		}
	}
}
