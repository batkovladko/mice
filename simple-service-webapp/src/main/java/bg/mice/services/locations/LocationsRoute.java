package bg.mice.services.locations;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.mice.data.dto.Elements;
import bg.mice.data.dto.LocationJSON;
import bg.mice.data.model.Location;
import bg.mice.data.model.LocationProperties;
import bg.mice.helpers.ResponseUtils;
import bg.mice.services.LocationService;

@Path("locations")
public class LocationsRoute extends LocationService {

	@Context
	private HttpServletRequest servletRequest;

	private Logger logger = LogManager.getLogger(this.getClass());

	@GET
	@Path("{locationId}")
	@Produces("application/json")
	public Response findById(@PathParam("locationId") final Long locationId) {
		final EntityManager em = getEm();
		final Location ep = em.find(Location.class, locationId);

		if (ep != null) {
			Response response = Response.ok().entity(ep).build();
			return response;
		} else {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_NOT_FOUND, "No Location with given id");
		}
	}

	@GET
	@Produces("application/json")
	public List<Elements> getIt(@Context UriInfo uri) {

		final EntityManager em = getEm();
		List<Elements> elements = new ArrayList<Elements>();
		MultivaluedMap<String, String> queryParams = uri.getQueryParameters();
		if (!queryParams.isEmpty()) {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Object> query = criteriaBuilder.createQuery();
			Root<LocationProperties> rootElement = query.from(LocationProperties.class);
			for (Entry<String, List<String>> queryParam : queryParams.entrySet()) {
				String value = queryParam.getValue().get(0);
				query.where(criteriaBuilder.equal(rootElement.get(queryParam.getKey()), value));
			}
			return null;
		} else {
			List<Location> locationPath = em.createNamedQuery("Location.findAllLocations", Location.class)
					.getResultList();
			for (Location location : locationPath) {
				elements.add(new Elements(location, em
						.createNamedQuery("LocationProperties.findPropertiesByParent", LocationProperties.class)
						.setParameter("locationid", location).getResultList()));
			}
			return elements;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@POST
	public Response add() throws IOException, IOException {
		InputStream imageFile = null;

		try {
			// Check that we have a file upload request
		
			LocationValidationResponse validationResult = validateInput(servletRequest, true);
			
			if (validationResult.getItems() == null) {
				return ResponseUtils.buildRestResponseWithBody(HttpURLConnection.HTTP_OK, "Metadata: errorCode="
						+ (validationResult.getError() != null ? validationResult.getError().getStatus() : "-1") + ";");
			}

			FileItem fileItem = getItemWithName(INPUT_FILE, validationResult.getItems());
			FileItem dataItem = getItemWithName(INPUT_FORM_DATA, validationResult.getItems());
			LocationJSON location = gson.fromJson(URLDecoder.decode(dataItem.getString(), "UTF-8"), LocationJSON.class);

			location.setImageName(fileItem.getName());
			imageFile = fileItem.getInputStream();

			persistNewLocation(location, imageFile);
			return ResponseUtils.buildRestResponseWithBody(HttpURLConnection.HTTP_CREATED, "success");
		} catch (Exception e) {
			logger.error("Error during adding location", e);
			return ResponseUtils.buildRestServerError(e.getMessage());
		} finally {
			if (imageFile != null) {
				imageFile.close();
			}
		}
	}

	

	@DELETE
	@Path("{id}")
	public Response delete(@PathParam("id") final Long id) {
		System.out.println("Deleting location with ID:" + id);
		if (id == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid location id : empty id");
		}
		EntityManager em = null;
		Location location = null;
		try {
			em = getEm();
			em.getTransaction().begin();
			location = em.find(Location.class, id);
			if (location == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Cannot delete location with id [%s] - it does not exist.", id));
			}

			List<LocationProperties> locationProperties = LocationProperties.byLocation(location, em);
			for (LocationProperties lProperty : locationProperties) {
				em.remove(lProperty);
			}

			em.remove(location);
			em.getTransaction().commit();
			return Response.ok(location).build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot delete property group [%s]", location) + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}
}
