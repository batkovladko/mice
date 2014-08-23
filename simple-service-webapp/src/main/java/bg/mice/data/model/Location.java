package bg.mice.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
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
	
	public Location() {
	}
	
	public Location(long id, String name, String description) {
	}
	
	
	public Location(long id, String name, String description, String imageName, Date created,
			List<LocationProperties> locationProperties) {
		this(id, name, description);
		this.imageName = imageName;
		this.created = created;
		this.locationProperties = locationProperties;
	}

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

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public static List<Location> getAll(EntityManager em) {
		return em.createNamedQuery("Location.findAllLocations", Location.class).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Location> getResult(String queryId, EntityManager em) {
		String query = "select location.* from location join "
				+ " (select distinct(l.l_id) from memory_table mt left join all_data l on l.l_id=mt.location_id"
				+ " where mt.query_n=? order by l.l_id, l.pg_name) result"
				+ " on location.id=result.l_id;";
		return em.createNativeQuery(query, Location.class).setParameter(1, queryId).getResultList();
	}
	
	@Override
	public String toString() {
		return "Location [id=" + id + ", name=" + name + ", description=" + description + ", imageName=" + imageName
				+ ", created=" + created + ", locationProperties=" + locationProperties + "]";
	}
	
}