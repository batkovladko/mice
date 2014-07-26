package org.persistence.dto;

/**
 * @author I057508
 * 
 */
public class LocationPropertiesDTO {

	private long id;
	private String name;
	private String value;
	private long locationId;
	private long propertyGroupId;

	public LocationPropertiesDTO() {
	}

	public LocationPropertiesDTO(long id, String name, String value, long locationId) {
		super();
		this.id = id;
		this.name = name;
		this.value = value;
		this.locationId = locationId;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public long getLocationId() {
		return locationId;
	}
	public long getPropertyGroupId() {
		return propertyGroupId;
	}
	
	public void setPropertyGroupId(long propertyGroupId) {
		this.propertyGroupId = propertyGroupId;
	}

	@Override
	public String toString() {
		return "LocationPropertiesDTO [id=" + id + ", name=" + name + ", value=" + value + ", locationId=" + locationId
				+ ", propertyGroupId=" + propertyGroupId + "]";
	}
}