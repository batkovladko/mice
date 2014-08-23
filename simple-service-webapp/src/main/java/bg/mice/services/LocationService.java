package bg.mice.services;

import static bg.mice.ApplicationContextListener.getPicturesFolder;
import static bg.mice.helpers.ResponseUtils.buildRestBadRequest;
import static bg.mice.helpers.ResponseUtils.buildRestCustomError;
import static bg.mice.helpers.ResponseUtils.buildRestServerError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import bg.mice.config.App;
import bg.mice.data.dto.LocationAddPropertiesDTO;
import bg.mice.data.dto.LocationAddDTO;
import bg.mice.data.model.Location;
import bg.mice.data.model.LocationProperties;
import bg.mice.data.model.PropertyGroups;
import bg.mice.services.locations.LocationValidationResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LocationService extends Route {
	public static final String INPUT_FILE = "file";
	public static final String INPUT_FORM_DATA = "formData";

	protected Gson gson = new GsonBuilder().create();

	protected LocationValidationResponse validateInput(HttpServletRequest request, boolean fileIsMandatory)
			throws FileUploadException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (!isMultipart && fileIsMandatory) {
			return new LocationValidationResponse(buildRestBadRequest("Missing file!"));
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();

		File repository = (File) request.getServletContext().getAttribute("javax.servlet.context.tempdir");
		factory.setRepository(repository);

		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(request);

		if (items.size() >= 2) {
			FileItem fileItem = getItemWithName(INPUT_FILE, items);
			if (fileItem == null && fileIsMandatory) {
				return new LocationValidationResponse(buildRestServerError("Missing file!"));
			}
			if (fileItem != null && fileItem.getSize() > App.MAX_FILE_SIZE) {
				String errorMessage = String.format("Too big image file. Max allowed [%d], actuall [%s]",
						App.MAX_FILE_SIZE, fileItem.getSize());
				return new LocationValidationResponse(buildRestCustomError(451, errorMessage));
			} else {
				FileItem formData = getItemWithName(INPUT_FORM_DATA, items);
				if (formData == null || StringUtils.isEmpty(formData.getString())) {
					return new LocationValidationResponse(buildRestServerError("Missing or empty formData"));
				} else {
					return new LocationValidationResponse(items);
				}
			}
		}
		return new LocationValidationResponse(null, null);
	}

	protected void persistNewLocation(LocationAddDTO inputLocation, InputStream image) throws IOException {
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

	protected void updateLocation(LocationAddDTO newLocation, InputStream image) throws FileNotFoundException,
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

	private void addLocationProperties(List<LocationAddPropertiesDTO> inputProperties, final Location newLocation,
			final EntityManager em) {
		if (inputProperties == null) {
			return;
		}

		for (LocationAddPropertiesDTO property : inputProperties) {
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
