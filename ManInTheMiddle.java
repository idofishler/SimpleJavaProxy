import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.ws.Response;


public class ManInTheMiddle {

	private static final int MAX_PACKET_SIZE = 100000;
	private InputStream hostInStream, clientInStream;
	private OutputStream hostOutStream, clientOutStream;
	private BufferedReader hostBufferedInputStream;
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

	public void go() {

		openHostSocket();
		openClientStreams();
		//forwardFirstRequestToHost();
		//processResponseHeaders();
		
		int dataToRead = 0, dataBeenRead = 0;

		// TODO handle additional request to the same host??

		try {
			
			hostOutStream.write(firstRequest.getBytes(), 0, firstRequest.getBytes().length);
			m_logger.log("Forwarding request to host....\n");
			hostOutStream.flush();

			ResponseHeadersProcessor responseHeadersProcessor = 
				new ResponseHeadersProcessor(hostBufferedInputStream);

			String rawResponseHeaders = responseHeadersProcessor.getRawResponse();
			m_logger.log("RawResponseHeaders:\n" + rawResponseHeaders);
			
			//send response header back to client
			m_logger.log("Forwarfing response headers to client...");
			clientOutStream.write(rawResponseHeaders.getBytes());
			clientOutStream.flush();

			if (responseHeadersProcessor.isChunked()) {
				dataToRead = getNextChunkSize();
			} else {
				dataToRead = responseHeadersProcessor.getContentLength();
			}
			
			byte[] buffer;

			while (dataToRead != 0) {
				
				m_logger.log("Data to read: " + dataToRead++);

				buffer = new byte[dataToRead];

				while (dataBeenRead <  dataToRead) {
					buffer[dataBeenRead++] = (byte) hostInStream.read();
				}
				
				m_logger.log("Forwarding: " + dataBeenRead + " bytes");
				clientOutStream.write(buffer);
				clientOutStream.flush();
				
				// reset counters for next chunk
				dataBeenRead = 0;
				dataToRead = getNextChunkSize();
			}
			hostSocket.close();	
		} 
		
		catch (IOException e) {
			m_logger.log(e);
		}
	}

	private int getNextChunkSize() throws IOException {
		StringBuilder sbCunckSize = new StringBuilder();
		StringBuilder orinal = new StringBuilder();
		char nextChar;
		nextChar = (char) hostInStream.read();
		while (Character.isWhitespace(nextChar)) {
			orinal.append(nextChar);
			nextChar = (char) hostInStream.read();
		}
		
		// read chunk size until next CRLF
		while (!Character.isWhitespace(nextChar)) {
			sbCunckSize.append(nextChar);
			orinal.append(nextChar);
			nextChar = (char) hostInStream.read();
		}
		clientOutStream.write(orinal.toString().getBytes());
		clientOutStream.flush();
		return Integer.parseInt(sbCunckSize.toString(), 16);
	}

	private void openHostSocket() { 

		try {
			hostSocket = new Socket(host, port);
			hostOutStream = hostSocket.getOutputStream();
			hostInStream = hostSocket.getInputStream();	
			hostBufferedInputStream = new BufferedReader(
					new InputStreamReader(hostInStream));
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
}
