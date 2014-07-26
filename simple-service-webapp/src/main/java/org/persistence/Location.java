package org.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

@Entity
@Table(name = "location")
@NamedQueries({ @NamedQuery(name = "Location.findAllLocations", query = "SELECT u FROM Location u",
hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) }) })

@TableGenerator(name = "locationGenerator", table = "sequence", pkColumnName = "seq_name",
pkColumnValue = "location_id", valueColumnName = "seq_count", allocationSize = 1)
public class Location {

	@GeneratedValue(generator = "locationGenerator", strategy = GenerationType.TABLE)
	@Id
	private long id;
	private String name;
	private String description;
	private String imageName;
	@Temporal(TemporalType.TIMESTAMP)
	private Date created = new Date();
	
	@OneToMany(mappedBy="location", fetch=FetchType.EAGER)
	List<LocationProperties> locationProperties = new ArrayList<LocationProperties>();
	
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	};
	
	public String getImageName() {
		return imageName;
	}
	
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	
	public List<LocationProperties> getLocationProperties() {
		return locationProperties;
	}
	
	public void setLocationProperties(List<LocationProperties> locationProperties) {
		this.locationProperties = locationProperties;
	}

//	@Override
//	public String toString() {
//		return "Location [id=" + id + ", name=" + name + ", description=" + description + ", imageName=" + imageName
//				+ ", created=" + created + ", locationProperties=" + locationProperties + "]";
//	}
	
}