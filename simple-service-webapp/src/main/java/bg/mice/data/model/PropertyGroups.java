package bg.mice.data.model;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

@XmlRootElement
@Entity
@Table(name = "propertygroups")
@NamedQueries({ @NamedQuery(name = "PropertyGroups.findAllGroups", query = "SELECT u FROM PropertyGroups u", 
hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) })
, @NamedQuery(name = "PropertyGroups.findGroupsExcludingConnectionOfType", query = "SELECT u FROM PropertyGroups u where u.connectedToOtherAs<>:connectedToOther", 
hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) }),

@NamedQuery(name = "PropertyGroups.findGroupsIncludingConnectionOfType", query = "SELECT u FROM PropertyGroups u where u.connectedToOtherAs =:connectedToOther", 
hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) })
		
})
@TableGenerator(name = "propertiesGroupGenerator", table = "sequence", pkColumnName = "seq_name", pkColumnValue = "properties_group_id", valueColumnName = "seq_count", allocationSize = 1)
public class PropertyGroups {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "propertiesGroupGenerator")
	private long id;
	@Basic
	private String name;
	@Basic
	private String type;
	
	@Enumerated(EnumType.STRING)
	@Column(name="CONNECTED_TO_OTHER")
	private ConnectionType connectedToOtherAs = ConnectionType.NOT_CONNECTED;

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
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

	public ConnectionType getConnectedToOtherAs() {
		return connectedToOtherAs;
	}

	public void setConnectedToOtherAs(ConnectionType connectedToOtherAs) {
		this.connectedToOtherAs = connectedToOtherAs;
	}
	
	public static enum ConnectionType {
		NOT_CONNECTED,
		PARENT,
		CHILD
	}

	@Override
	public String toString() {
		return "PropertyGroups [id=" + id + ", name=" + name + ", type=" + type + ", connected_to_other_as: " + connectedToOtherAs + " ]";
	}

	public static List<PropertyGroups> findGroupsExcludingConnectionOfType(ConnectionType excludeConnectionType, final EntityManager em) {
		return em
				.createNamedQuery("PropertyGroups.findGroupsExcludingConnectionOfType", PropertyGroups.class)
				.setParameter("connectedToOther", excludeConnectionType).getResultList();
	}
	
	public static List<PropertyGroups> findGroupsIncludingConnectionOfType(ConnectionType includeConnectionType, final EntityManager em) {
		return em
				.createNamedQuery("PropertyGroups.findGroupsIncludingConnectionOfType", PropertyGroups.class)
				.setParameter("connectedToOther", includeConnectionType).getResultList();
	}

}