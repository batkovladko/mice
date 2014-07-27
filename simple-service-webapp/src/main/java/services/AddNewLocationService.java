package services;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.filterapp.services.LocationJSON;

/**
 * Servlet implementation class FIleUploader
 */
@WebServlet("/api/v1/addnewlocation")
public class AddNewLocationService extends LocationService {
	
	private static final long serialVersionUID = 1L;
	
	private	Logger logger = LogManager.getLogger(AddNewLocationService.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddNewLocationService() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		InputStream imageFile = null;
		try {
			// Check that we have a file upload request

			List<FileItem> items = validateInput(request, response, true);
			if (items == null) {
				response.getWriter().println("Metadata: errorCode=" + response.getStatus() + ";");
				return;
			}
			
			FileItem fileItem = getItemWithName(INPUT_FILE, items);
			FileItem dataItem = getItemWithName(INPUT_FORM_DATA, items);
			LocationJSON location = gson.fromJson(URLDecoder.decode(dataItem.getString(), "UTF-8"),
					LocationJSON.class);

			location.setImageName(fileItem.getName());
			imageFile = fileItem.getInputStream();

			persistNewLocation(location, imageFile);
			response.getWriter().println("success");
			response.setStatus(HttpURLConnection.HTTP_CREATED);
		} catch (Exception e) {
			logger.error("Error during adding location", e);
			buildServerError(response, e.getMessage());
		} finally {
			if (imageFile != null) {
				imageFile.close();
			}
		}
	}
}
