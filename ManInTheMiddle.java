import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.text.AbstractDocument.BranchElement;


public class ManInTheMiddle {

	private InputStream hostInStream, clientInStream;
	private OutputStream hostOutStream, clientOutStream;
	private String m_host;
	private int m_hostPort;
	private Socket clientSocket, hostSocket;
	private static Logger m_logger = new Logger();
	private static Logger m_errorLogger = new Logger(System.err);

	public ManInTheMiddle(InputStream clientIn, OutputStream clientOut) {
		this.m_host = "";
		this.m_hostPort = -1;
		this.clientInStream = clientIn;
		this.clientOutStream = clientOut;
	}

	public void go(String host, int port, String rawRequest) {

		// will only open if host or port are new.
		openHostSocket(host, port);

		int dataToRead = 0, dataBeenRead = 0;

		try {

			hostOutStream.write(rawRequest.getBytes(), 0, rawRequest.getBytes().length);
			m_logger.log("Forwarding request to host....\n");
			//hostOutStream.flush();

			ResponseHeadersProcessor responseHeadersProcessor = 
				new ResponseHeadersProcessor(hostInStream);

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

			while (dataToRead > 0) {

				if (responseHeadersProcessor.isChunked()) {
					dataToRead += proxyServer.CRLF.length();					
				}

				m_logger.log("Data to read: " + dataToRead);

				buffer = new byte[dataToRead];

				while (dataBeenRead <  dataToRead) {
					buffer[dataBeenRead++] = (byte) hostInStream.read();
				}

				m_logger.log("Forwarding: " + dataBeenRead + " bytes");
				clientOutStream.write(buffer);
				clientOutStream.flush();

				// reset counters for next chunk
				dataBeenRead = 0;
				
				if (!responseHeadersProcessor.isChunked() ||
						responseHeadersProcessor.isCloseConnection()) {
					dataToRead = 0;
				} else {
					dataToRead = getNextChunkSize();					
				}

			}
			hostOutStream.flush();
//			hostOutStream.close();
//			hostInStream.close();
		} 

		catch (IOException e) {
			m_errorLogger.log(e);
		}
	}

	private int getNextChunkSize() throws IOException {
		StringBuilder sbCunckSize = new StringBuilder();
		char nextChar;
		int result = -1;

		while ((nextChar = (char) hostInStream.read()) != -1) {

			sbCunckSize.append(nextChar);
			clientOutStream.write(nextChar);

			if (sbCunckSize.toString().endsWith(proxyServer.CRLF)) {
				try {
					result = Integer.parseInt(sbCunckSize.toString().trim(), 16);
					break;
				} catch (NumberFormatException nfe) {
					m_logger.log("can't parse chunck size");
				}
			}
		}
		return result;
	}

	/**
	 * Will open new socket IFF host or port are different.
	 * to keep persistent 
	 * @param host the server host name
	 * @param port the server port
	 */
	private void openHostSocket(String host, int port) { 
		try {
			// when the same host and port
			if (host.equalsIgnoreCase(m_host) && m_hostPort == port) {
				// only reopen the input and output streams
				hostInStream = hostSocket.getInputStream();
				hostOutStream = hostSocket.getOutputStream();
				return;
			} else {
				if (hostSocket != null) {
					hostSocket.close();
				}
				m_host = host;
				m_hostPort = port;

				hostSocket = new Socket(host, port);
				hostOutStream = hostSocket.getOutputStream();
				hostInStream = hostSocket.getInputStream();
			}
		}

		catch (UnknownHostException e) {
			m_errorLogger.log(e, "Don't know about host: " + host);
		} catch (IOException e) {
			m_errorLogger.log(e, "Couldn't get I/O for "
					+ "the connection to: " + host);
		}
	}

	private void openClientStreams() {
		try {
			clientOutStream = clientSocket.getOutputStream();
			clientInStream = clientSocket.getInputStream();	
		} catch (IOException e) {
			m_errorLogger.log(e, "Couldn't get I/O for "
					+ "the connection to: " + m_host);
		}
	}
}
