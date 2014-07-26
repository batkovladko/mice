package services;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.filterapp.services.LocationJSON;
import bg.filterapp.services.ResponseUtils;

public class UpdateLocationService extends LocationService {
	private static final long serialVersionUID = -8492948004736493003L;
	private static Logger logger = LogManager.getLogger(UpdateLocationService.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		InputStream imageFile = null;

		try {
			List<FileItem> items = validateInput(request, response, false);

			if (items == null) {
				return;
			}

			String decodedFormData = URLDecoder.decode(getItemWithName(INPUT_FORM_DATA, items).getString(), "UTF-8");
			LocationJSON location = gson.fromJson(decodedFormData, LocationJSON.class);

			FileItem fileItem = getItemWithName(INPUT_FILE, items);
			
			if (fileItem != null) {
				location.setImageName(fileItem.getName());
				imageFile = fileItem.getInputStream();
			}

			updateLocation(location, imageFile);
			response.setStatus(HttpURLConnection.HTTP_OK);
			response.getWriter().println("success");
		} catch (FileUploadException e) {
			logger.error("Error during update of location.", e);
			buildServerError(response, e.getMessage());
			response.getWriter().println(response.getHeader(ResponseUtils.ERROR_MESSAGE_HEADER));
		} catch (BadRequestException bre) {
			logger.error("Error during update of location.", bre);
			buildBadRequest(response, bre.getMessage());
			response.getWriter().println(response.getHeader(ResponseUtils.ERROR_MESSAGE_HEADER));
		}
	}
}
