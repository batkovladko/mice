package bg.mice.helpers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class ResponseUtils {
	public static final String ERROR_MESSAGE_HEADER = "x-errormsg";
	public static final String ERROR_MESSAGE_ADD_PARAM_HEADER = "x-additionalparam";

	public static Response buildResponseWithHeader(int statusCode, String errorMessage) {
		ResponseBuilder rb = Response.status(statusCode);
		if (errorMessage != null)
			rb.header(ERROR_MESSAGE_HEADER, errorMessage);
		return rb.build();
	}
	
	public static Response buildResponseWithHeader(int statusCode, String errorMessage, String additionalParam) {
		ResponseBuilder rb = Response.status(statusCode);
		if (errorMessage != null)
			rb.header(ERROR_MESSAGE_HEADER, errorMessage);
		
		if (additionalParam != null) {
			rb.header(ERROR_MESSAGE_ADD_PARAM_HEADER, additionalParam);
		}
		
		return rb.build();
	}
}
