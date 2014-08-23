package bg.mice.data.dto;

import java.util.List;

public class LocationJSON {

	private long id;
	private String name;
	private String description;
	private String imageName;

	private List<LocationAddPropertiesJSON> properties;

	public LocationJSON() {

	}

	public LocationJSON(final String name, final List<LocationAddPropertiesJSON> properties) {
		this.name = name;
		this.properties = properties;
	}
	
	
	public LocationJSON(String name, String description, List<LocationAddPropertiesJSON> properties) {
		super();
		this.name = name;
		this.description = description;
		this.properties = properties;
	}
	

	public LocationJSON(long id, String name, String description, String imageName,
			List<LocationAddPropertiesJSON> properties) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.imageName = imageName;
		this.properties = properties;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<LocationAddPropertiesJSON> getProperties() {
		return properties;
	}

	public void setProperties(List<LocationAddPropertiesJSON> properties) {
		this.properties = properties;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getImageName() {
		return imageName;
	}
	
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
}
