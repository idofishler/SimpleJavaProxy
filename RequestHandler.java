import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.sql.Time;
import java.util.Hashtable;


public class RequestHandler implements Runnable {

	private static final String SHOW_DETAILS = "showDetails=1";
	private static final int SHOW_HEADERS = 1;
	private static final int FORBIDDEN = 2;
	private static final int MANAGEMENT = 3;
	private static final int LOG = 4;
	private static final int MAN_IN_THE_MIDDLE = 0;
	private Socket m_socket;
	private PolicyFile m_policyFile;
	private Hashtable<String, String> m_headers;
	private RequestHeadersProcessor m_headersProcessor;
	private ManInTheMiddle m_manInTheMiddle;
	private static Logger m_logger = new Logger();
	private static Logger m_errorLogger = new Logger(System.err);

	public RequestHandler(Socket connectionSocket, PolicyFile policyFile) {
		m_socket = connectionSocket;
		m_policyFile = policyFile;
	}

	@Override
	public void run() {
		try {
			InputStream inputStream = m_socket.getInputStream();
			OutputStream outputStream = m_socket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream, true);
			
			m_manInTheMiddle = new ManInTheMiddle(inputStream, outputStream);

			int status;

			while (m_socket.isConnected()) {
				
				status = readRequest(inputStream);
				
				switch (status) {

				case FORBIDDEN:
					show403page(printWriter);
					break;
				case SHOW_HEADERS:
					showHeadersPage(printWriter);
					break;
				case MANAGEMENT:
					showManagementPage();
					break;
				case LOG:
					showLogPage();
					break;
				case MAN_IN_THE_MIDDLE:
					doManInTheMiddle();
					break;

				default:
					m_logger.log("Could not handle request");
				}
			}
			

			m_socket.close();
			m_logger.log("End of comunication");
			m_logger.log("===========================================================");

		} catch (MalformedURLException e) {
			m_errorLogger.log(e, "Problem with the URL");
		} catch (IOException ioe) {
			m_errorLogger.log(ioe);
		}
	}

	private int readRequest(InputStream inputStream) throws IOException {

		m_headersProcessor = new RequestHeadersProcessor(inputStream);

		// load this fields here
		m_headers = m_headersProcessor.getRequestHeaders();

		if (isShowDeatails()) {
			return SHOW_HEADERS;
		}

		// showDetails is OFF
		else {

			// Check policy
			String rule = isForbbiden();
			if (!"".equalsIgnoreCase(rule)) {
				logForbiddenRequest(rule);
				return FORBIDDEN;
			}

			URL url = new URL(m_headers.get(RequestHeadersProcessor.URL));
			String host = url.getHost();
			if (url.toString().equalsIgnoreCase("http://content-proxy/management")) {
				return MANAGEMENT;
			}

			else if (url.toString().equalsIgnoreCase("http://content-proxy/log")) {
				return LOG;
			}

			// normal proxy mode
			else {
				return MAN_IN_THE_MIDDLE;
			}
		}
	}

	private void showHeadersPage(PrintWriter outputStream) throws MalformedURLException {
		String html = m_headersProcessor.showHeadersSummery();
		outputStream.println(html);
	}

	private void show403page(PrintWriter outputStream) {
		StringBuilder result = new StringBuilder();

		result.append("<html>\n<body>\n")
		.append("<h1>HTTP Status 403 - Access is denied</h1>\n")
		.append("</body>\n</html>");

		outputStream.println(result);

		outputStream.flush();
	}

	private void showManagementPage() {
		// TODO change management page to receive input and output stream and not socket
		ManagementPage managementPage = new ManagementPage(m_socket, m_policyFile);
		String query = m_headers.get(RequestHeadersProcessor.QUERY);
		managementPage.go(query);
	}

	private void showLogPage() throws IOException {

		PrintWriter outputStream = new PrintWriter(
				m_socket.getOutputStream(), true);
		StringBuilder result = new StringBuilder();

		result.append("<html>\n<body>\n")
		.append("<h1>Bolocked Request Log</h1>\n")
		.append("<p>" + BlockedRequestLogger.getInstance().getLog() + "</p>\n")
		.append("</body>\n</html>");

		outputStream.println(result);

		outputStream.flush();
	}

	private void doManInTheMiddle() throws MalformedURLException {

		// Getting the information needed for making a decision
		String rawRequest = m_headersProcessor.getRawRequest();
		URL url = new URL(m_headers.get(RequestHeadersProcessor.URL));
		String host = url.getHost();
		int port = url.getPort();
		if (port == -1) port = 80;

		// Starting man in the middle and let it do it's job
		
		m_manInTheMiddle.go(host, port, rawRequest);
	}

	private String isForbbiden() throws MalformedURLException {
		URL url = new URL(m_headers.get(RequestHeadersProcessor.URL));
		String host = url.getHost();
		String path = url.getPath();

		// Policy checks
		for (String rule : m_policyFile.getBlockedHosts()) {
			if (rule.contains(host)) {
				return (PolicyFile.BLOCK_SITE + " \"" + rule + "\"");
			}
		}
		for (String rule : m_policyFile.getBlockedResorces()) {
			if (path.endsWith(rule)) {
				return (PolicyFile.BLOCK_RESOURCE + " \"" + rule + "\"");
			}
		}
		return "";
	}

	private boolean isShowDeatails() throws MalformedURLException {
		String query = m_headersProcessor.getQuery();
		return (query != null && query.contains(SHOW_DETAILS));
	}

	private void logForbiddenRequest(String rule) {
		StringBuilder result = new StringBuilder();
		result.append("Time: " + (new Time(System.currentTimeMillis())) + "<BR>\n")
		.append("Blocked Request to: " + m_headers.get(RequestHeadersProcessor.URL) + "<BR>\n")
		.append("The rule: " + rule + "<BR>\n")
		.append("==========================================================<BR>\n");
		BlockedRequestLogger.getInstance().log(result.toString());
	}
}
