package org.persistence.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author I057508
 * 
 */
public class LocationDTO {

	private long id;
	private String name;
	private String description;
	private Date created;
	private List<LocationPropertiesDTO> locationProperties = new ArrayList<LocationPropertiesDTO>();

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public void setName(final String param) {
		this.name = param;
	}

	public String getName() {
		return name;
	}

	public void setLocationProperties(List<LocationPropertiesDTO> locationProperties) {
		this.locationProperties = locationProperties;
	}

	public List<LocationPropertiesDTO> getLocationProperties() {
		return locationProperties;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		LocationDTO objDTO = (LocationDTO) obj;
		return objDTO.getId() == this.id;
	}

	@Override
	public int hashCode() {
		return (int) this.id * 1;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public String toString() {
		return "LocationDTO [id=" + id + ", name=" + name + ", description=" + description + ", locationProperties="
				+ locationProperties + "]";
	}

}