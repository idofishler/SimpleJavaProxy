import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;


public class ResponseHeadersProcessor {

	private static final String PROTOCOL = "protocol";
	private static final String RESPONSE_CODE = "response_code";
	private static final String RESPONSE_TEXT = "response_text";
	private static final String CHUNK_SIZE = "chunk_size";
	private BufferedReader in;
	private static Logger m_logger = new Logger();
	private Hashtable<String, String> m_headers;
	private String m_rawResponse;

	public ResponseHeadersProcessor(BufferedReader bufferedInputStream) {
		in = bufferedInputStream;
		m_headers = getResponseHeaders();
	}

	private Hashtable<String, String> getResponseHeaders() {
		if (m_headers != null) {
			return m_headers;
		}

		Hashtable<String, String> result = new Hashtable<String, String>();
		StringBuilder rawResponse = new StringBuilder();
		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null) {

				rawResponse.append(inputLine + "\n");

				if ("".equalsIgnoreCase(inputLine)) {
						break;
				}
				
				// first line handle
				if (inputLine.indexOf("HTTP") > -1) {
					String[] firstLineArgs = inputLine.split(" ");
					result.put(PROTOCOL, firstLineArgs[0]);
					result.put(RESPONSE_CODE, firstLineArgs[1]);
					result.put(RESPONSE_TEXT, firstLineArgs[2]);
				} 

				// other parameters
				else {
					String[] parametr = inputLine.split(": ");
					if (parametr.length == 2) {
						result.put(parametr[0], parametr[1]);						
					} else {
						result.put(inputLine, inputLine);
					}
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
			return Integer.parseInt(m_headers.get("Content-Length"));			
		} else {
			return -1;
		}
	}
	
	public boolean isChunked() {
		return "chunked".equalsIgnoreCase(m_headers.get("Transfer-Encoding"));
	}
	
}

