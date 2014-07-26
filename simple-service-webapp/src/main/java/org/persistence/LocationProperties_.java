package org.persistence;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2014-02-05T00:18:11.297+0200")
@StaticMetamodel(LocationProperties.class)
public class LocationProperties_ {
	public static volatile SingularAttribute<LocationProperties, Long> id;
	public static volatile SingularAttribute<LocationProperties, String> value;
	public static volatile SingularAttribute<LocationProperties, Location> location;
	public static volatile SingularAttribute<LocationProperties, Property> properties;
}
