import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class ManInTheMiddle {

	private static final int MAX_PACKET_SIZE = 10000;
	private InputStream hostInStream, clientInStream;
	private OutputStream hostOutStream, clientOutStream;
	private String host;
	private int port;
	private Socket clientSocket, hostSocket;
	private String firstRequest;
	private static Logger m_logger = new Logger();

	public ManInTheMiddle(String host, int port, String request, Socket clientSocket) {
		this.host = host;
		this.port = port;
		this.firstRequest = request;
		this.clientSocket = clientSocket;
	}

	private void openHostSocket() { 

		try {
			hostSocket = new Socket(host, port);
			hostOutStream = hostSocket.getOutputStream();
			hostInStream = hostSocket.getInputStream();	
		} catch (UnknownHostException e) {
			m_logger.log(e, "Don't know about host: " + host);
		} catch (IOException e) {
			m_logger.log(e, "Couldn't get I/O for "
					+ "the connection to: " + host);
		}
	}

	private void openClientStreams() {
		try {
			clientOutStream = clientSocket.getOutputStream();
			clientInStream = clientSocket.getInputStream();	
		} catch (IOException e) {
			m_logger.log(e, "Couldn't get I/O for "
					+ "the connection to: " + host);
		}
	}

	public void go() {

		openHostSocket();
		openClientStreams();

		byte[] buffer = new byte[MAX_PACKET_SIZE];
		try {
			// TODO handle additional request to the same host
			int n = 0; //clientInStream.read(buffer);
			
			hostOutStream.write(firstRequest.getBytes(), 0, firstRequest.getBytes().length);
			m_logger.log("Forwarding request to host");
			hostOutStream.flush();
			
			do {
				n = hostInStream.read(buffer);
				
				// DEBUG
				System.out.println("Reciving " + n + " bytes");
				
				if (n > 0) {
					clientOutStream.write(buffer, 0, n);
				} 
			} while (n > 0);
			
			clientOutStream.flush();
			
			hostSocket.close();
			
		} catch (IOException e) {
			m_logger.log(e, "Problem with communication between browser and host");
		}
	}
}
