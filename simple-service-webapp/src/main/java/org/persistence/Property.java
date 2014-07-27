package org.persistence;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

@Entity
@Table(name = "properties")

@NamedQueries({ @NamedQuery(name = "Properties.findPropertiesByPropertyGroups", 
							query = "SELECT b FROM Property b WHERE b.propertyGroups = :propertygroup",
							hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) }),
				@NamedQuery(name = "Properties.findPropertyByConnectedFilter", 
							query = "SELECT b FROM Property b WHERE b.childPropertyGroups = :childPropertyGroups",
							hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) }),
				@NamedQuery(name = "Properties.findPropertiesByPropertyGroupAndNonEmptyChildPropertyGroups", 
							query = "SELECT b FROM Property b WHERE b.propertyGroups= :propertyGroups and b.childPropertyGroups != null",
							hints = { @QueryHint(name = QueryHints.REFRESH, value = HintValues.TRUE) })
})
@TableGenerator(name = "propertiesGenerator", table = "sequence", pkColumnName = "seq_name", pkColumnValue = "properties_id", valueColumnName = "seq_count", allocationSize = 1)
public class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "propertiesGenerator")
	private Long id;
	@Column(length = 255, nullable = false)
	private String value;

	@XmlTransient
	@ManyToOne
	private PropertyGroups propertyGroups;
	@XmlTransient
	@ManyToOne
	@JoinColumn(name="CHILD_PROPERTYGROUPS_ID")
	private PropertyGroups childPropertyGroups;
	public static final int PROPERTY_VALUE_MAX_LENGTH = 255;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setValue(final String param) {
		this.value = param;
	}

	public String getValue() {
		return value;
	}

	public PropertyGroups getPropertyGroups() {
		return propertyGroups;
	}

	public void setPropertyGroups(final PropertyGroups param) {
		this.propertyGroups = param;
	}
	
	public PropertyGroups getChildPropertyGroups() {
		return childPropertyGroups;
	}

	public void setChildPropertyGroups(PropertyGroups childPropertyGroups) {
		this.childPropertyGroups = childPropertyGroups;
	}

	public static List<Property> byPropertyGroup(PropertyGroups pGroup, EntityManager em) {
		return em.createNamedQuery("Properties.findPropertiesByPropertyGroups", Property.class)
				.setParameter("propertygroup", pGroup).getResultList();
	}
	
	public static List<Property> byPropertyGroupAndNonEmptyChildPropertyGroups(PropertyGroups pGroup, EntityManager em) {
		return em.createNamedQuery("Properties.findPropertiesByPropertyGroupAndNonEmptyChildPropertyGroups", Property.class)
				.setParameter("propertyGroups", pGroup).getResultList();
	}
	
	
	public static Property byChildPropertyGroup(PropertyGroups pGroup, EntityManager em) {
		try { 
			return em.createNamedQuery("Properties.findPropertyByConnectedFilter", Property.class).setParameter("childPropertyGroups", pGroup).getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "Property [id=" + id + ", value=" + value + ", propertyGroups=" + propertyGroups
				+ ", child Property GorupId=" + (childPropertyGroups != null ? childPropertyGroups.getName()
				: "<unkown>") + "]";
	}
}