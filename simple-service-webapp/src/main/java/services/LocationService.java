package services;

import static bg.filterapp.ApplicationContextListener.getPicturesFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.persistence.Location;
import org.persistence.LocationProperties;
import org.persistence.PropertyGroups;

import bg.filterapp.services.LocationAddPropertiesJSON;
import bg.filterapp.services.LocationJSON;
import bg.filterapp.services.Route;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LocationService extends HttpServlet {
	public static final String INPUT_FILE = "file";
	public static final String INPUT_FORM_DATA = "formData";

	private static final long serialVersionUID = 1L;

	static final int MAX_FILE_SIZE = 1024 * 1024 * 2;
	protected Gson gson = new GsonBuilder().create();

	protected List<FileItem> validateInput(HttpServletRequest request, HttpServletResponse response,
			boolean fileIsMandatory) throws FileUploadException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (!isMultipart && fileIsMandatory) {
			buildBadRequest(response, "Missing file!");
			return null;
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletContext servletContext = this.getServletConfig().getServletContext();

		File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
		factory.setRepository(repository);

		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(request);

		if (items.size() >= 2) {
			FileItem fileItem = getItemWithName(INPUT_FILE, items);
			if (fileItem == null && fileIsMandatory) {
				buildServerError(response, "Missing file!");
				return null;
			}
			if (fileItem != null && fileItem.getSize() > MAX_FILE_SIZE) {
				buildServerError(response, "Too big image file : " + fileItem.getSize());
				return null;
			} else {
				FileItem formData = getItemWithName(INPUT_FORM_DATA, items);
				if (formData == null || StringUtils.isEmpty(formData.getString())) {
					buildServerError(response, "Missing or empty formData");
					return null;
				} else {
					return items;
				}
			}
		}
		return null;
	}

	protected void persistNewLocation(LocationJSON inputLocation, InputStream image) throws IOException {
		final EntityManager em = Route.getEm();
		em.getTransaction().begin();

		try {
			final Location newLocation = new Location();

			newLocation.setName(inputLocation.getName());
			newLocation.setDescription(inputLocation.getDescription());
			newLocation.setImageName(inputLocation.getImageName());
			em.persist(newLocation);
			if (image != null && image.available() > 0) {
				File out = new File(getPicturesFolder() + inputLocation.getImageName());
				System.out.println("Writing file " + out.getAbsolutePath());
				IOUtils.copy(image, new FileOutputStream(out));
			}
			System.out.println("New Location");
			addLocationProperties(inputLocation.getProperties(), newLocation, em);
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}
	}

	protected void updateLocation(LocationJSON newLocation, InputStream image) throws FileNotFoundException,
			IOException {
		final EntityManager em = Route.getEm();
		em.getTransaction().begin();

		try {
			Location oldLocation = em.find(Location.class, newLocation.getId());

			if (oldLocation == null) {
				throw new BadRequestException(String.format(
						"Cannot update the location with id [%s]. Such location does not exist.", newLocation.getId()));
			}

			oldLocation.setName(newLocation.getName());
			oldLocation.setDescription(newLocation.getDescription());

			if (image != null && image.available() > 0) {
				oldLocation.setImageName(newLocation.getImageName());
				File out = new File(getPicturesFolder() + newLocation.getImageName());
				System.out.println(String.format("Saving image file [%s] for location [%s]", out.getAbsolutePath(),
						oldLocation.getId()));
				IOUtils.copy(image, new FileOutputStream(out));
			}

			for (LocationProperties p : oldLocation.getLocationProperties()) {
				System.out.println("Deleting property :" + p.getId() + ":" + p.getValue());
				p.setLocation(null);
				em.remove(p);
			}
			oldLocation.setLocationProperties(new ArrayList<LocationProperties>());

			System.out.println(String.format("Updating Location [%s]", oldLocation.getId()));
			addLocationProperties(newLocation.getProperties(), oldLocation, em);
			em.getTransaction().commit();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}

	}

	private void addLocationProperties(List<LocationAddPropertiesJSON> inputProperties, final Location newLocation,
			final EntityManager em) {
		if (inputProperties == null) {
			return;
		}

		for (LocationAddPropertiesJSON property : inputProperties) {
			if (StringUtils.isEmpty(property.getValue())) {
				continue;
			}
			long pGroupId = Long.parseLong(property.getName());

			LocationProperties locationProperty = new LocationProperties();

			locationProperty.setValue(property.getValue());
			locationProperty.setLocation(newLocation);

			PropertyGroups pGroup = em.find(PropertyGroups.class, pGroupId);
			if (pGroup == null) {
				throw new IllegalArgumentException(String.format("Mising property group [%]", pGroupId));
			}
			locationProperty.setGroup(pGroup);
			em.persist(locationProperty);
			newLocation.getLocationProperties().add(locationProperty);
		}
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
		buildServerError(response, error, true);
	}

	public static void buildServerError(HttpServletResponse response, String error, boolean printInTheResponse)
			throws IOException {
		response.addHeader("error", error);
		if (printInTheResponse) {
			response.getWriter().println(error);
		}
		response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
	}

	protected FileItem getItemWithName(String name, List<FileItem> items) {
		FileItem result = null;
		for (FileItem fileItem : items) {
			if (name.equals(fileItem.getName()) || name.equals(fileItem.getFieldName())) {
				result = fileItem;
				break;
			}
		}
		return result;
	}

}
