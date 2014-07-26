package org.persistence;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.QueryHint;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.tomcat.jni.Address;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import bg.filterapp.services.JsonIgnore;
import bg.filterapp.services.JsonIgnoreOnSerialization;

@Entity
@Table(name = "locationproperties")
@NamedStoredProcedureQuery(name = "filter", resultClasses = Address.class, procedureName = "filter", parameters = {
		@StoredProcedureParameter(mode = ParameterMode.IN, name = "P_QUERY_N", type = String.class),
		@StoredProcedureParameter(mode = ParameterMode.IN, name = "P_INPUT_FILTER", type = String.class) })
@NamedQueries({
		@NamedQuery(name = "LocationProperties.findPropertiesByParent", query = "SELECT b FROM LocationProperties b WHERE b.location = :locationid", hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) }),
		@NamedQuery(name = "LocationProperties.findByPropertyGroup", query = "SELECT b FROM LocationProperties b WHERE b.group= :pGroup", hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) }),
		@NamedQuery(name = "LocationProperties.findByLocation", query = "SELECT b FROM LocationProperties b WHERE b.location= :pLocation", hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) }),
		@NamedQuery(name = "LocationProperties.findByLocationandGroup", query = "SELECT b FROM LocationProperties b WHERE b.location= :pLocation and b.group= :pGroup", hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE)})
})
@TableGenerator(name = "locationPropertiesGenerator", table = "sequence", pkColumnName = "seq_name", pkColumnValue = "location_p_id", valueColumnName = "seq_count", allocationSize = 1)
public class LocationProperties {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "locationPropertiesGenerator")
	private long id;
	@Basic
	private String value;

	@ManyToOne
	@JoinColumn(name = "LOCATION_ID")
	@JsonIgnore
	@JsonIgnoreOnSerialization
	private Location location;

	@ManyToOne
	@JoinColumn(name = "P_GROUP_ID")
	private PropertyGroups group;

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public void setValue(String param) {
		this.value = param;
	}

	public String getValue() {
		return value;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location param) {
		this.location = param;
	}

	public PropertyGroups getGroup() {
		return group;
	}

	public void setGroup(PropertyGroups group) {
		this.group = group;
	}

	public static List<LocationProperties> byPropertyGroup(PropertyGroups pGroup, EntityManager em) {
		return em.createNamedQuery("LocationProperties.findByPropertyGroup", LocationProperties.class)
				.setParameter("pGroup", pGroup).getResultList();
	}

	public static List<LocationProperties> byLocation(Location location, EntityManager em) {
		return em.createNamedQuery("LocationProperties.findByLocation", LocationProperties.class)
				.setParameter("pLocation", location).getResultList();
	}
	public static List<LocationProperties> byLocationAndGroup(Location location, PropertyGroups pGroup, EntityManager em) {
		return em.createNamedQuery("LocationProperties.findByLocationandGroup", LocationProperties.class)
				.setParameter("pLocation", location).setParameter("pGroup", pGroup).getResultList();
	}
}