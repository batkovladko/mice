package bg.filterapp.services;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.persistence.Location;
import org.persistence.LocationProperties;

@XmlRootElement
public class Elements {
	 private Location location;
	 private List<LocationProperties> properties;
	 public Elements() {}
	 public Elements(final Location location, final List<LocationProperties> properties)
	 {
		 this.location = location; this.properties = properties; 
	 }
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public List<LocationProperties> getProperties() {
		return properties;
	}
	public void setProperties(List<LocationProperties> properties) {
		this.properties = properties;
	}
	 
}
