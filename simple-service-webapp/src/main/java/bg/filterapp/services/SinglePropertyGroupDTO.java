package bg.filterapp.services;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.persistence.LocationProperties;
import org.persistence.Property;
import org.persistence.PropertyGroups;

@XmlRootElement
public class SinglePropertyGroupDTO {

	private long id;

	private String name;

	private String type;
	private String value;

	private List<Property> properties;
	
	private List<String> locationPropertyValues;

	public SinglePropertyGroupDTO() {
	}

	public SinglePropertyGroupDTO(final PropertyGroups propertyGroup,
			final List<Property> list, List<LocationProperties> locationPropertyList) {
		this(locationPropertyList);
		this.id = propertyGroup.getId();
		this.name = propertyGroup.getName();
		this.type = propertyGroup.getType();
		this.properties = list;
	}

	public SinglePropertyGroupDTO(List<LocationProperties> locationProperties) {
		List<String> result = new ArrayList<String>();
		if (locationProperties == null) {
			return;
		}
		for (LocationProperties lp : locationProperties) {
			result.add(lp.getValue());
		}
		this.locationPropertyValues = result;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
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

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getLocationPropertyValues() {
		return locationPropertyValues;
	}

	public void setLocationPropertyValues(List<String> locationPropertyValues) {
		this.locationPropertyValues = locationPropertyValues;
	}
}
