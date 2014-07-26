package bg.filterapp.services;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.persistence.Location;
import org.persistence.LocationProperties;
import org.persistence.Property;
import org.persistence.PropertyGroups;
import org.persistence.PropertyGroups.ConnectionType;

@Path("propertygroups")
public class PropertyGroupsRoute extends Route {

	@GET
	@Produces("application/json")
	public List<SinglePropertyGroupDTO> getIt(
			@QueryParam(value = "excludeConnectionType") PropertyGroups.ConnectionType excludeConnectionType,
			@QueryParam(value = "includeConnectionType") PropertyGroups.ConnectionType inclConnectionType
			) {

		if (excludeConnectionType != null && inclConnectionType != null) {
			ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, "Please use either include or exclude parameter");
		}
		final EntityManager em = getEm();

		List<PropertyGroups> propertyGroups = null;
		if (excludeConnectionType != null) {
			System.out.println(String.format("Querying for filters (property group) different than [%s]",
					excludeConnectionType));
			propertyGroups = PropertyGroups.findGroupsExcludingConnectionOfType(excludeConnectionType, em);

		} else if (inclConnectionType != null) {
			System.out.println(String.format("Querying for filters (property group) of type [%s]",
					inclConnectionType));
			propertyGroups = PropertyGroups.findGroupsIncludingConnectionOfType(inclConnectionType, em);
		} else if (excludeConnectionType == null && inclConnectionType == null){
			System.out.println(String.format("Querying for all filters (property group)", excludeConnectionType));
			propertyGroups = em.createNamedQuery("PropertyGroups.findAllGroups", PropertyGroups.class).getResultList();
		}

		List<SinglePropertyGroupDTO> pg = new ArrayList<SinglePropertyGroupDTO>();
		for (PropertyGroups singleGroup : propertyGroups) {
			pg.add(new SinglePropertyGroupDTO(singleGroup, Property.byPropertyGroup(singleGroup, em), null));
		}
		return pg;
	}

	@GET
	@Produces("application/json")
	@Path("{id}")
	/**
	 * Fetches certain property group(filter). Optionally (if specified) returns it's properties(attributes) for certain location.
	 * @param id
	 * @param locationId
	 * @return
	 */
	public Response getIt(@PathParam("id") Long id, @QueryParam(value="locationId") Long locationId) {
		final EntityManager em = getEm();
		PropertyGroups pGroup = em.find(PropertyGroups.class, id);
		List<LocationProperties> locationPropertyValues = Collections.emptyList();
		
		if (pGroup != null) {
			System.out.println("Found " + pGroup);
			if (locationId != null) {
				Location location = em.find(Location.class, locationId.longValue());
				if (location == null) {
					return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
								"Invalid location id : "+ locationId);
				}
				locationPropertyValues = LocationProperties.byLocationAndGroup(location, pGroup, em);
				
			}
			return Response.ok(new SinglePropertyGroupDTO(pGroup, Property.byPropertyGroup(pGroup, em), locationPropertyValues)).build();
		} else {
			System.out.println("Nothing is found with id " + id);
			return null;
		}
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public MessageResponse postGroup(final PropertiesJSON pr) {
		final EntityManager em = getEm();

		em.getTransaction().begin();
		try {
			final PropertyGroups newGroup = new PropertyGroups();
			newGroup.setName(pr.getName());
			newGroup.setType(pr.getType());

			em.persist(newGroup);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}

		return new MessageResponse(true);
	}

	@PUT
	@Consumes("application/json")
	@Path("{id}")
	public Response update(@PathParam("id") final Long id, final PropertyGroups inPropertyGroup) {
		System.out.println("Updating property group with ID:" + id + " and values: " + inPropertyGroup);
		if (id == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid  property group id : empty id");
		}
		EntityManager em = null;
		try {
			em = getEm();
			em.getTransaction().begin();
			PropertyGroups pGroup = em.find(PropertyGroups.class, id);
			if (pGroup == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Property group with id [%s] does not exist.", id));
			}
			if (StringUtils.isEmpty(inPropertyGroup.getName())) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						"Empty property group name");
			}
			pGroup.setName(inPropertyGroup.getName());
			em.getTransaction().commit();
			return Response.ok().build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot update property group [%s]", inPropertyGroup) + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	@DELETE
	@Path("{id}")
	public Response delete(@PathParam("id") final Long id) {
		System.out.println("Deleting property group with ID:" + id);
		if (id == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid property group id : empty id");
		}
		EntityManager em = null;
		PropertyGroups pGroup = null;
		try {
			em = getEm();
			em.getTransaction().begin();
			pGroup = em.find(PropertyGroups.class, id);
			if (pGroup == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Cannot delete property group with id [%s] - it does not exist.", id));
			}

			List<LocationProperties> locationProperties = LocationProperties.byPropertyGroup(pGroup, em);
			if (!locationProperties.isEmpty()) {
				return ResponseUtils.buildResponseWithHeader(410, String.format(
						"Cannot delete property group with id [%s]. "
								+ "This group is used for at least one property of an existing location.", id));
			}
			List<Property> properties = Property.byPropertyGroup(pGroup, em);
			for (Property property : properties) {
				em.remove(property);
			}

			em.remove(pGroup);
			em.getTransaction().commit();
			return Response.ok(pGroup).build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot delete property group [%s]", pGroup) + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	@DELETE
	@Path("{id}/connection")
	public Response deleteConnection(@PathParam("id") final Long id) {
		System.out.println("Deleting connection for filter(property group) with ID: " + id);
		if (id == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid property id : empty id");
		}
		EntityManager em = null;
		Property property = null;

		try {
			em = getEm();
			em.getTransaction().begin();

			PropertyGroups childPGroup = em.find(PropertyGroups.class, id);
			if (childPGroup == null || !ConnectionType.CHILD.equals(childPGroup.getConnectedToOtherAs())) {
				String errorMessage = String
						.format("A  property(value) cannot be disconnected to Property Group [%s] because such group does not exist or"
								+ " it is not a child group at all!",
								id);
				System.out.println(errorMessage);
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, errorMessage);
			}

			property = Property.byConnectedFilter(childPGroup, em);

			if (property == null) {
				String errorMessage = String
						.format("[inconsistency]: Cannot delete connection of the filter(property group) [%s] "
								+ "with its property , because the filter is not connected to any property, although it is considered as a PARENT. ",
								id);
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, errorMessage);
			}
			
			if (!property.getPropertyGroups().getConnectedToOtherAs().equals(ConnectionType.PARENT)) {
				String errorMessage = String
						.format("[inconsistency]: Cannot delete connection of the filter(property group) [%s] "
								+ "with its property , because the filter's type is not PARENT ",
								property.getPropertyGroups().getId());
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, errorMessage);
			}

			System.out.println(String.format(
					"Deleting connection between attribute(property) [%s] and filter(property group) [%s]: ", id,
					property.getChildPropertyGroups().getId()));

			childPGroup.setConnectedToOtherAs(ConnectionType.NOT_CONNECTED);
			property.setChildPropertyGroups(null);
			property.getPropertyGroups().setConnectedToOtherAs(ConnectionType.NOT_CONNECTED);
			em.getTransaction().commit();
			return Response.ok(property).build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot delete connection for property [%s]", property) + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}
}
