package bg.mice.services;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import bg.mice.data.model.LocationProperties;
import bg.mice.data.model.Property;
import bg.mice.data.model.PropertyGroups;
import bg.mice.data.model.PropertyGroups.ConnectionType;
import bg.mice.helpers.ResponseUtils;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("property")
public class PropertyRoute extends Route {

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response addAttribute(final Property inProperty) throws URISyntaxException {
		System.out.println("Adding attribute:" + inProperty);
		EntityManager em = null;
		Property newProp = null;
		try {
			if (StringUtils.isEmpty(inProperty.getValue())) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						"Empty property value.");
			}
			if (inProperty.getValue().length() > Property.PROPERTY_VALUE_MAX_LENGTH) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Invalid property value. Length > [%d]", Property.PROPERTY_VALUE_MAX_LENGTH));
			}

			if (inProperty.getPropertyGroups() == null || inProperty.getPropertyGroups().getId() == 0) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						"Invalid property group.");
			}

			if (inProperty.getChildPropertyGroups() != null
					&& !PropertyGroups.ConnectionType.NOT_CONNECTED.equals(inProperty.getChildPropertyGroups()
							.getConnectedToOtherAs())) {
				System.out.println(String.format("A new property(value) cannot be connected to Property Group [%s], "
						+ "which is not free for connection (it is either a child or parent",
						inProperty.getChildPropertyGroups()));

				return ResponseUtils
						.buildResponseWithHeader(
								HttpURLConnection.HTTP_BAD_REQUEST,
								"A new property(value) cannot be connected to Property Group, which is not free for connection (it is either a child or parent");
			}

			em = getEm();
			em.getTransaction().begin();

			PropertyGroups pGroup = em.find(PropertyGroups.class, inProperty.getPropertyGroups().getId());
			if (pGroup == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, String.format(
						"Property group with id [%s] does not exist.", inProperty.getPropertyGroups().getId()));
			}

			newProp = new Property();
			newProp.setValue(inProperty.getValue());
			newProp.setPropertyGroups(pGroup);
			em.persist(newProp);
			em.getTransaction().commit();
		} catch (Exception e) {
			// TODO switch to logger
			System.out.println("Cannot add new property." + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
					System.out.println("Cannot add new property. Transaction rolled back");
				}
				em.close();
			}
		}

		return Response.created(new URI("property/" + newProp.getId())).entity(newProp).build();
	}

	@GET
	@Produces("application/json")
	@Path("/bygroup/{id}")
	public Response byGroupId(@PathParam("id") final Long pGroupId) {
		System.out.println("Attribute byGroupID:" + pGroupId);
		if (pGroupId == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid group id : empty value");
		}
		EntityManager em = null;
		try {
			em = getEm();
			PropertyGroups pGroup = em.find(PropertyGroups.class, pGroupId);
			if (pGroup == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Property group with id [%s] does not exist.", pGroupId));
			}
			TypedQuery<Property> query = em.createNamedQuery("Properties.findPropertiesByPropertyGroups", Property.class)
					.setParameter("propertygroup", pGroup);
			List<Property> properties = query.getResultList();
			return Response.ok(properties).build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot get properties for group [%s]", pGroupId) + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	@PUT
	@Consumes("application/json")
	@Path("{id}")
	public Response update(@PathParam("id") final Long id, final Property inProperty) {
		System.out.println("Updating attribute with ID:" + id + " and values: " + inProperty);
		if (id == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid property id : empty id");
		}
		EntityManager em = null;
		try {
			em = getEm();
			em.getTransaction().begin();
			Property property = em.find(Property.class, id);
			if (property == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Property with id [%s] does not exist.", id));
			}
			if (StringUtils.isEmpty(inProperty.getValue())) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						"Empty property value.");
			}

			property.setValue(inProperty.getValue());
			// TODO find all LocationProperties with the same "old" value and
			// update them also!!!
			em.getTransaction().commit();
			return Response.ok().build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot update property [%s]", inProperty) + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	@PUT
	@Path("{id}/connection/{pGroupId}")
	public Response update(@PathParam("id") final Long id, @PathParam("pGroupId") final Long pGroupId) {
		System.out.println("Connecting attribute(property) with ID [" + id + "] to filter(propertygroup) [" + pGroupId
				+ "]");
		if (id == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid property id.");
		}
		if (pGroupId == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid property group id.");
		}
		EntityManager em = null;
		try {
			em = getEm();
			em.getTransaction().begin();

			PropertyGroups childPGroup = em.find(PropertyGroups.class, pGroupId);
			if (childPGroup == null || !PropertyGroups.ConnectionType.NOT_CONNECTED.equals(childPGroup.getConnectedToOtherAs())) {
				System.out.println(String.format("A property(value) cannot be connected to Property Group [%s]"
						+ " or to group which is not free for connection (it is either a child or parent", childPGroup));
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, String
						.format("A property(value) cannot be connected to unexisting Property Group"
								+ " or to group which is not free for connection (it is either a child or parent"));
			}

			Property property = em.find(Property.class, id);
			if (property == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Property with id [%s] does not exist.", id));
			}
			
			if (property.getPropertyGroups().getConnectedToOtherAs().equals(ConnectionType.CHILD)) {
				String errorMessage = String.format("The corresponding filter [%s] for this property [%s] is a child one", property.getPropertyGroups(), id);
				System.out.println(errorMessage);
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						errorMessage);
			}
			
			if (property.getPropertyGroups().getId() == childPGroup.getId()) {
				String errorMessage = String.format("Cannot connect a filter as a child to to its attributes ", property.getPropertyGroups(), id);
				System.out.println(errorMessage);
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						errorMessage);
			}
			if (property.getChildPropertyGroups() != null) {
				String errorMessage = String.format("The property [%s] already have a child filter [%s]. ", property, property.getChildPropertyGroups());
				System.out.println(errorMessage);
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST, errorMessage, "occupied");
			}
			
			property.setChildPropertyGroups(childPGroup);
			property.getPropertyGroups().setConnectedToOtherAs(ConnectionType.PARENT);
			childPGroup.setConnectedToOtherAs(ConnectionType.CHILD);
			em.getTransaction().commit();
			return Response.ok().build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot connect property [%s]", id) + e.getMessage());
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
		System.out.println("Deleting attribute with ID:" + id);
		if (id == null) {
			return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
					"Invalid property id : empty id");
		}
		EntityManager em = null;
		Property property = null;
		try {
			em = getEm();
			em.getTransaction().begin();
			property = em.find(Property.class, id);
			if (property == null) {
				return ResponseUtils.buildResponseWithHeader(HttpURLConnection.HTTP_BAD_REQUEST,
						String.format("Cannot delete property with id [%s] - it does not exist.", id));
			}
			if (property.getChildPropertyGroups() != null) {
				return ResponseUtils.buildResponseWithHeader(411, String.format(
						"Cannot delete property  with id [%s]. " + "This property has associated filter [id=%s] with.",
						id, property.getChildPropertyGroups().getId()));
			}
			List<LocationProperties> locationProperties = LocationProperties.byPropertyGroup(
					property.getPropertyGroups(), em);
			if (!locationProperties.isEmpty()) {
				return ResponseUtils.buildResponseWithHeader(410, String.format(
						"Cannot delete property  with id [%s]. "
								+ "This group is used for at least one property of an existing location.", id));
			}
			em.remove(property);
			em.getTransaction().commit();
			return Response.ok(property).build();
		} catch (Exception e) {
			System.out.println(String.format("Cannot delete property [%s]", property) + e.getMessage());
			e.printStackTrace(System.out);
			return Response.serverError().build();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}
}