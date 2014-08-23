package bg.mice.services.locations;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.mice.data.dto.LocationAddDTO;
import bg.mice.helpers.ResponseUtils;
import bg.mice.services.LocationService;


/**
 * <pre>
 * This is a workaround of the following problem:
 * HTML forms cannot be submitted with method type different than GET & POST.
 * Only http requests done via XmlHttpRequest object can support all HTTP METHODS, 
 * but the update of the location is quite complicated to be submitted in that way.
 * This is why another REST resource is being introduced, which will has POST method serving the actual update of the resource.
 * </pre>
 * TODO - check whether the location-view-edit.view.html form can be submitted with the XmlHttpRequest object.
 * @author I057508
 *
 */
@Path("update-locations")
public class UpdateLocationsRoute extends LocationService {

	@Context
	private HttpServletRequest servletRequest;

	private Logger logger = LogManager.getLogger(this.getClass());

	@POST
	public Response update() throws IOException {
		InputStream imageFile = null;

		try {
			LocationValidationResponse validationResult = validateInput(servletRequest, false);

			if (validationResult.getItems() == null) {
				return ResponseUtils.buildRestResponseWithBody(HttpURLConnection.HTTP_OK, "Metadata: errorCode="
						+ (validationResult.getError() != null ? validationResult.getError().getStatus() : "-1") + ";");
				//return Response.ok().build();
			}

			String decodedFormData = URLDecoder.decode(getItemWithName(INPUT_FORM_DATA, validationResult.getItems()).getString(), "UTF-8");
			LocationAddDTO location = gson.fromJson(decodedFormData, LocationAddDTO.class);

			FileItem fileItem = getItemWithName(INPUT_FILE, validationResult.getItems());

			if (fileItem != null) {
				location.setImageName(fileItem.getName());
				imageFile = fileItem.getInputStream();
			}

			updateLocation(location, imageFile);
			return ResponseUtils.buildRestResponseWithBody(HttpURLConnection.HTTP_OK, "success");
		} catch (FileUploadException e) {
			logger.error("Error during update of location.", e);
			return ResponseUtils.buildRestServerError(e.getMessage());
		} catch (BadRequestException bre) {
			logger.error("Error during update of location.", bre);
			return ResponseUtils.buildRestBadRequest(bre.getMessage());
		}
	}
}
