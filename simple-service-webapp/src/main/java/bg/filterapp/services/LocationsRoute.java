package bg.filterapp.services;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.persistence.Location;
import org.persistence.LocationProperties;

@Path("locations")
public class LocationsRoute extends Route {

	@Context
	private HttpServletRequest servletRequest;
	@Context
	private HttpServletResponse servletResponse;

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

	// @POST
	// @Consumes("application/json")
	// public Response postLocation(final LocationJSON locationJson) {
	// final EntityManager em = getEm();
	// em.getTransaction().begin();
	// try {
	// final Location newLocation = new Location();
	// newLocation.setName(locationJson.getName());
	// em.persist(newLocation);
	// for (LocationAddPropertiesJSON property : locationJson
	// .getProperties()) {
	// LocationProperties locationProperty = new LocationProperties();
	// locationProperty.setValue(property.getValue());
	// locationProperty.setLocation(newLocation);
	// locationProperty.setGroup(em.find(PropertyGroups.class,
	// Long.parseLong(property.getName())));
	// em.persist(locationProperty);
	// }
	// em.getTransaction().commit();
	// return Response.ok().build();
	// } catch (Exception e) {
	// e.printStackTrace();
	// em.getTransaction().rollback();
	// return Response.serverError().build();
	// }
	// }

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
