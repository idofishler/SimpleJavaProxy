import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;


public class RequestHeadersPresentor {
	
	private BufferedReader in;
	private PrintWriter out;
	private static Logger logger = new Logger();

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
		String rawRequest = getRawRequest();
		
		URL rawURL = new URL(rawRequest);
		
		result.append("<html><body>\n")
		.append("<--! Ido Fisler & Dor Tumarkin -->\n")
		.append("Browser sent a request to: " + rawURL.getHost() + "\n")
		.append("Used HTTP Request Method: " + rawRequest.substring(0, 3) + "\n")
		.append("Resource Requested: " + rawURL.getPath() + "\n")
		
		
		.append("</body></html>");


		// debug
		System.out.println(result);
		System.out.println("RequestHeaderPresentor Done!");
		
		return result.toString();
	}
	

	private String getRawRequest() {
		StringBuilder result = new StringBuilder();
		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null) {
				if ("".equalsIgnoreCase(inputLine)) {
					break;
				}
				
				result.append(inputLine);
				
			}
			
		} catch (IOException ioe) {
			logger.log(ioe);
		}

		// debug
		System.out.println(result);
		
		return result.toString();
	}
}
