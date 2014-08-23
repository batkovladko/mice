package bg.mice.helpers;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletResponse;
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
	
	public static Response buildRestResponseWithBody(int statusCode, String body) {
		ResponseBuilder rb = Response.status(statusCode);
		rb.entity(body);
		return rb.build();
	}

	public static Response buildRestServerError(String error) throws IOException {
		return ResponseUtils.buildRestServerError(error, true);
	}

	public static Response buildRestServerError(String error, boolean printInTheResponse)
			throws IOException {
		ResponseBuilder rb  = Response.serverError();
		rb.header("error", error);
		if (printInTheResponse) {
			rb.entity(error);
		}
		return rb.build();
	}

	public static Response buildRestBadRequest(String error) throws IOException {
		ResponseBuilder rb  = Response.status(HttpURLConnection.HTTP_BAD_REQUEST);
		rb.header("error", error);
		rb.encoding(error);
		return rb.build();
	}

	public static Response buildRestCustomError(int statusCode, String error) {
		ResponseBuilder rb  = Response.status(statusCode);
		if (error != null) {
			rb.header(ERROR_MESSAGE_HEADER, error);
		}
		return rb.build();
	}

	public static void buildBadRequest(HttpServletResponse response, String error) throws IOException {
		response.addHeader("error", error);
		response.getWriter().println(error);
		response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
	}

	public static void buildNotFound(HttpServletResponse response, String error) throws IOException {
		response.addHeader("error", error);
		response.getWriter().println(error);
		response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
	}

	public static void buildServerError(HttpServletResponse response, String error) throws IOException {
		ResponseUtils.buildServerError(response, error, true);
	}

	public static void buildCustomError(int statusCode, String error, HttpServletResponse response) {
		response.setStatus(statusCode);
		if (error != null) {
			response.addHeader(ERROR_MESSAGE_HEADER, error);
		}
	}

	public static void buildServerError(HttpServletResponse response, String error, boolean printInTheResponse)
			throws IOException {
		response.addHeader("error", error);
		if (printInTheResponse) {
			response.getWriter().println(error);
		}
		response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
	}
}
