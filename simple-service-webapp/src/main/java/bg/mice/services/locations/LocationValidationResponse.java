package bg.mice.services.locations;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;

public class LocationValidationResponse {
	private final List<FileItem> items;
	private final Response error;

	public LocationValidationResponse(List<FileItem> items, Response error) {
		super();
		this.items = items;
		this.error = error;
	}

	public LocationValidationResponse(Response error) {
		this(null, error);
	}

	public LocationValidationResponse(List<FileItem> items) {
		this(items, null);
	}

	public List<FileItem> getItems() {
		return items;
	}

	public Response getError() {
		return error;
	}
}
