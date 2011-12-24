import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;


public class RequestHeadersPresentor {
	
	private static final String METHOD = "method";
	private static final String URL = "url";
	private static final String PROTOCOL = "protocol";
	
	private BufferedReader in;
	private PrintWriter out;
	private static Logger m_logger = new Logger();

	// TODO: remove???
	public RequestHeadersPresentor(BufferedReader inputStream,
			PrintWriter outputStream) {
		in = inputStream;
		out = outputStream;
	}
	
	public RequestHeadersPresentor(BufferedReader inputStream) {
		in = inputStream;
		out = null;
	}

	public String procces() throws MalformedURLException {
		StringBuilder result = new StringBuilder();
		
		Hashtable<String, String> headers = getRequestHeaders();
		URL url = new URL(headers.get(URL));
		
		result.append("<html><body>\n")
		.append("<!-- Ido Fisler & Dor Tumarkin -->\n")
		.append("Used HTTP Request Method: " + headers.get(METHOD) + "<BR>\n")
		.append("Browser sent a request to: " + url.getHost() + "<BR>\n")
		.append("Request Protocol: " + headers.get(PROTOCOL) + "<BR>\n")
		.append("Resource Requested: " + url.getPath() + "<BR>\n")
		.append("Parameters:<BR>\n")
		.append(getParametersTable(url))
		.append("HTTP Headers:<BR>\n")
		.append(getHeadersTable(headers))
		.append("</body></html>");

		// debug
		System.out.println(result);
		System.out.println("RequestHeaderPresentor Done!");
		
		return result.toString();
	}
	

	private Hashtable<String, String> getRequestHeaders() {
		Hashtable<String, String> result = new Hashtable<String, String>();
		StringBuilder rawRequest = new StringBuilder();
		try {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				// TODO: remove this to get data - use length
				if ("".equalsIgnoreCase(inputLine)) {
					break;
				} else if (inputLine.contains("GET") || inputLine.contains("POST")){
					String[] firstLineArgs = inputLine.split(" ");
					result.put(METHOD, firstLineArgs[0]);
					result.put(URL, firstLineArgs[1]);
					result.put(PROTOCOL, firstLineArgs[2]);
				} else {
					String[] parametr = inputLine.split(" ");
					if (parametr.length == 2) {
						result.put(parametr[0], parametr[1]);						
					} else {
						result.put(inputLine, inputLine);
					}
				}
				rawRequest.append(inputLine + "\n");	
			}
		} catch (IOException ioe) {
			m_logger.log(ioe);
		}

		// debug
		m_logger.log("RawRequest:\n" + rawRequest.toString());
		m_logger.log("==========================================");
		
		return result;
	}

	private String getHeadersTable(Hashtable<String, String> headers) {
		
		StringBuilder result = new StringBuilder();
		
		Enumeration<String> parameters = headers.keys();
		
		result.append("<TABLE BORDER=1>\n" +
				"<TR>\n" +
                "<TH>Pareameter Name<TH>Parameter Value");
	
		while (parameters.hasMoreElements()) {
			String parameter = parameters.nextElement();
			String parametrValue = headers.get(parameter); 
			if (URL.equalsIgnoreCase(parameter) || METHOD.equalsIgnoreCase(parameter)
					|| PROTOCOL.equalsIgnoreCase(parameter)) {
				continue;
			}
			result.append("<TR><TD>" + parameter)
				.append("<TD>" + parametrValue);
		}
		
		result.append("</TABLE><BR>\n");
		
		return result.toString();
	}

	private String getParametersTable(URL rawURL) {
		StringBuilder result = new StringBuilder();
		String query = rawURL.getQuery();
		if (query != null) {
			StringTokenizer tokenizer = new StringTokenizer(query, "&");
			result.append("<TABLE BORDER=1>\n" +
				"<TR>\n" +
                "<TH>Pareameter Name<TH>Parameter Value");
			while (tokenizer.hasMoreElements()) {
				String param = tokenizer.nextToken();
				String[] nameValue = param.split("=");
				String paramName = nameValue[0];
				String paramValue = nameValue[1];
				result.append("<TR><TD>" + paramName)
					.append("<TD>" + paramValue);
			}
			result.append("</TABLE><BR>\n");        			
		}
		return result.toString();
	}
}
