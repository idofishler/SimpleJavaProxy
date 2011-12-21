import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;


public class RequestHandler implements Runnable {

	private Socket m_socket;
	private static Logger m_logger = new Logger();

	public RequestHandler(Socket connectionSocket) {
		m_socket = connectionSocket;
	}

	@Override
	public void run() {
		try {
			readRequest();

		} catch (IOException ioe) {
			m_logger.log(ioe);
		}
	}

	private void readRequest() throws IOException {
		BufferedReader inputStream = new BufferedReader(
				new InputStreamReader(m_socket.getInputStream()));
		PrintWriter outputStream = new PrintWriter(
				m_socket.getOutputStream(), true);

		// mark the beginning of the inputStream
		inputStream.mark(1024);
		
		if (isShowDeatails(inputStream)) {

			//reset the inputStream because it has been read by isShowDetails
			inputStream.reset(); 
			
			RequestHeadersPresentor headersPresentor = new RequestHeadersPresentor(inputStream);
			try {
				String headers = headersPresentor.procces();
				outputStream.println(headers);
			} catch (MalformedURLException e) {
				m_logger.log(e, "Problem with the URL");
			}
			
			
		}
		// showDetails is OFF
		else {
			// TODO: next section 
		}

		inputStream.close();
		outputStream.close();
		m_socket.close();

	}

	private boolean isShowDeatails(BufferedReader in) {
		String line;
		try {
			
			while ((line = in.readLine()) != null) {
				if ("".equalsIgnoreCase(line)) {
					break;
				}
				if (line.indexOf("showDetails=1") > 0) {
					m_logger.log("DEBUG: ShowDetails is ON");
					return true;
				}
			}
			m_logger.log("DEBUG: ShowDetails is OFF");
			return false;
			
		} catch (IOException ioe) {
			m_logger.log(ioe);
		}
		return true;
	}
}
