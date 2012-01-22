import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.sound.sampled.Line;


public class RequestHeadersProcessor {

	public static final String QUERY = "Query";
	public static final String METHOD = "method";
	public static final String URL = "url";
	public static final String PROTOCOL = "protocol";
	public static final String POST = "POST";
	public static final String GET = "GET";
	public static final Object CONTENT_LENGTH = "Content-Length";
	
	private static final String CRLF = proxyServer.CRLF;

	private InputStream in;
	private static Logger m_logger = new Logger();
	private Hashtable<String, String> m_headers;
	private String m_rawRequest;

	public RequestHeadersProcessor(InputStream inputStream) {
		in = inputStream;
		m_headers = getRequestHeaders();
	}

	public String showHeadersSummery() throws MalformedURLException {
		StringBuilder result = new StringBuilder();

		URL url = new URL(m_headers.get(URL));
		int requestLength = in.toString().getBytes().length;
		boolean isPost = m_headers.get(METHOD).equalsIgnoreCase(POST);

		// Response headers
		result.append(m_headers.get(PROTOCOL) + " 200 OK\n")
		.append("Date: " + (new Date(System.currentTimeMillis())) + "\n")
		.append("Content-Type: text/html\n\n");

		// Response data
		result.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">\n")
		.append("<!-- Ido Fisler & Dor Tumarkin -->\n")
		.append("<html><body>\n")
		.append("<H3>Used HTTP Request Method: " + m_headers.get(METHOD) + "</H3><BR>\n")
		.append("<H3>Browser sent a request to: " + url.getHost() + "</H3><BR>\n")
		.append("<H3>Request Protocol: " + m_headers.get(PROTOCOL) + "</H3><BR>\n")
		.append("<H3>Resource Requested: " + url.getPath() + "</H3><BR>\n")
		.append("<B>Parameters:</B><BR>\n")
		.append(getParametersTable())
		.append("<B>HTTP Headers:</B><BR>\n")
		.append(getHeadersTable())
		.append("<B>Data: the request contains </B>" + requestLength + " bytes.<BR>\n")
		.append("<B>Request query: </B>" + ((isPost) ? m_headers.get(QUERY) : url.getQuery()) + "<BR>\n")
		.append("</body></html>");

		return result.toString();
	}


	public Hashtable<String, String> getRequestHeaders() {

		if (m_headers != null) {
			return m_headers;
		}

		Hashtable<String, String> result = new Hashtable<String, String>();
		StringBuilder rawRequest = new StringBuilder();
		StringBuilder nextLine = new StringBuilder(); 
		String method;
		int CRLFcount = 0;
		try {
			char nextChar;
			String line;
			// TODO fix post handling.
			while ((nextChar = (char) in.read()) != -1) {

				rawRequest.append(nextChar);
				nextLine.append(nextChar);
				
				// find end of request
				if (rawRequest.toString().endsWith(CRLF+CRLF)) {
					method = result.get(METHOD);
					if (POST.equalsIgnoreCase(method)) {
						int dataLen = Integer.parseInt(result.get(CONTENT_LENGTH));
						String postData = getPostData(dataLen);
						result.put(QUERY, postData);
						break;
					} else {
						break;
					}
				}
				
				// after reading a full line
				else if ((line = nextLine.toString()).endsWith(CRLF)) {
					// TODO throw exception for connect requests...
					if (line.contains(GET) || line.contains(POST)) {
						String[] firstLineArgs = line.split(" ");
						result.put(METHOD, firstLineArgs[0]);
						result.put(URL, firstLineArgs[1]);
						result.put(PROTOCOL, firstLineArgs[2].trim());
					} 
					else {
						String[] parametr = line.split(": ");
						if (parametr.length == 2) {
							result.put(parametr[0], parametr[1].trim());						
						} else {
							result.put(line.trim(), line.trim());
						}
					}
					// reset nextLine
					nextLine = new StringBuilder();
				}
			}
		} catch (IOException ioe) {
			m_logger.log(ioe);
		}

		m_rawRequest = rawRequest.toString();
		
		// debug
		m_logger.log("RawRequest:\n" + m_rawRequest);

		return result;
	}

	public String getQuery() throws MalformedURLException {
		String method = m_headers.get(METHOD);
		URL url = new URL(m_headers.get(URL));
		String query = "";
		String queryFromUrl = url.getQuery();
		String queryFromData = m_headers.get(QUERY);

		query = queryFromUrl;

		if (POST.equalsIgnoreCase(method)) {
			if (queryFromData != null) {
				if (queryFromUrl != null) {
					query = queryFromUrl.concat("&" + queryFromData);
				} else {
					query = queryFromData;
				}
			}
		}
		return query;
	}
	
	public String getRawRequest() {
		return m_rawRequest;
	}

	private String getPostData(int data) throws IOException {
		char[] query = new char[data];
		for (int i = 0; i < data; i++) {
			query[i] = (char) in.read();
		}
		return new String(query);
	}

	private String getHeadersTable() {

		StringBuilder result = new StringBuilder();

		Enumeration<String> parameters = m_headers.keys();

		result.append("<TABLE BORDER=1>\n" +
				"<TR BGCOLOR=\"#FFAD00\">\n" +
		"<TH>Pareameter Name<TH>Parameter Value");

		while (parameters.hasMoreElements()) {
			String parameter = parameters.nextElement();
			String parametrValue = m_headers.get(parameter); 
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

	private String getParametersTable()
	throws MalformedURLException {

		StringBuilder result = new StringBuilder();
		String query = getQuery();
		if (query != null && !"".equalsIgnoreCase(query)) {
			StringTokenizer tokenizer = new StringTokenizer(query, "&");
			result.append("<TABLE BORDER=1>\n" +
					"<TR BGCOLOR=\"#FFAD00\">\n" +
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
