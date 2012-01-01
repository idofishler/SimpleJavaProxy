import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;


public class RequestHandler implements Runnable {

	private static final String SHOW_DETAILS = "showDetails=1";
	private Socket m_socket;
	private PolicyFile m_policyFile;
	private static Logger m_logger = new Logger();

	public RequestHandler(Socket connectionSocket, PolicyFile policyFile) {
		m_socket = connectionSocket;
		m_policyFile = policyFile;
	}

	@Override
	public void run() {
		try {
			// use status to keep persistency.
			int status = readRequest();

		} catch (IOException ioe) {
			m_logger.log(ioe);
		}
	}

	private int readRequest() throws IOException {
		BufferedReader inputStream = new BufferedReader(
				new InputStreamReader(m_socket.getInputStream()));
		PrintWriter outputStream = new PrintWriter(
				m_socket.getOutputStream(), true);

		inputStream.mark(1500);

		RequestHeadersProcessor headersProcessor = new RequestHeadersProcessor(inputStream);

		inputStream.reset();

		Hashtable<String, String> headers = headersProcessor.getRequestHeaders();
		String query = headersProcessor.getQuery();

		if (isShowDeatails(query)) {
			try {
				String html = headersProcessor.showHeadersSummery();
				outputStream.println(html);
				return 1;
			} catch (MalformedURLException e) {
				m_logger.log(e, "Problem with the URL");
			}
		}
		
		// showDetails is OFF
		else {
			
			// Getting the information needed for man in the middle
			URL url = new URL(headers.get(RequestHeadersProcessor.URL));
			String host = url.getHost();
			int port = url.getPort();
			if (port == -1) port = 80;
			String path = url.getPath();
//			int indexOfDot = path.indexOf('.');
//			String resource = "";
//			if (indexOfDot != -1) {
//				resource = path.substring(indexOfDot);
//			}
			
			String rawRequest = headersProcessor.getRawRequest();
			
			// Policy checks
			for (String rule : m_policyFile.getBlockedHosts()) {
				if (rule.contains(host)) {
					return403page(host);
					return 2;
				}
			}
			for (String rule : m_policyFile.getBlockedResorces()) {
				if (path.endsWith(rule)) {
					return403page();
					return 2;
				}
			}

			// Starting man in the middle and let it do it's job
			ManInTheMiddle theMan = new ManInTheMiddle(host, port, rawRequest, m_socket);
			theMan.go();

		}

		m_socket.close();
		m_logger.log("End of comunication");
		m_logger.log("===========================================================");
		return 0;
	}

	private void return403page() {
		// TODO Auto-generated method stub
		System.out.println("403!!!!!!");
	}

	private void return403page(String host) {
		// TODO Auto-generated method stub
		System.out.println("403!!!!!!");
	}

	private boolean isShowDeatails(String query) {
		return (query != null && query.contains(SHOW_DETAILS));
	}
}
