import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;


public class ResponseHeadersProcessor {

	private static final String PROTOCOL = "protocol";
	private static final String RESPONSE_CODE = "response_code";
	private static final String RESPONSE_TEXT = "response_text";
	private static final String CHUNK_SIZE = "chunk_size";
	private static Logger m_logger = new Logger();
	private static final String CRLF = proxyServer.CRLF;

	private InputStream in;
	private Hashtable<String, String> m_headers;
	private String m_rawResponse;

	public ResponseHeadersProcessor(InputStream hostInStream) {
		in = hostInStream;
		m_headers = getResponseHeaders();
	}

	private Hashtable<String, String> getResponseHeaders() {
		if (m_headers != null) {
			return m_headers;
		}

		Hashtable<String, String> result = new Hashtable<String, String>();
		StringBuilder rawResponse = new StringBuilder();
		StringBuilder nextLine = new StringBuilder(); 
		String method, line;
		char nextChar;
		
		try {
			while ((nextChar = (char) in.read()) != -1) {

				rawResponse.append(nextChar);
				nextLine.append(nextChar);

				// end of response
				if (rawResponse.toString().endsWith(CRLF+CRLF)) {
					break;
				}

				// reached end of line in header
				else if ((line = nextLine.toString()).endsWith(CRLF)) {
					
					// first line handle
					if (line.indexOf("HTTP") > -1) {
						String[] firstLineArgs = line.split(" ");
						result.put(PROTOCOL, firstLineArgs[0]);
						result.put(RESPONSE_CODE, firstLineArgs[1]);
						result.put(RESPONSE_TEXT, firstLineArgs[2]);
						
					// other parameters
					} else {
						String[] parametr = line.split(": ");
						if (parametr.length == 2) {
							result.put(parametr[0], parametr[1]);						
						} else {
							result.put(line, line);
						}
					}	
					nextLine = new StringBuilder();
				}
			}
			
		} catch (IOException e) {
			m_logger.log(e);
		}
		
		m_rawResponse = rawResponse.toString();
		return result;
	}

	public String getRawResponse() {
		return m_rawResponse;
	}
	
	public int getContentLength() {
		if (m_headers.containsKey("Content-Length")) {
			return Integer.parseInt(m_headers.get("Content-Length").trim());			
		} else {
			return -1;
		}
	}
	
	public boolean isChunked() {
		if (m_headers.containsKey("Transfer-Encoding")) {
			return "chunked".equalsIgnoreCase(m_headers.get("Transfer-Encoding").trim());
		}
		return false;
	}
}

