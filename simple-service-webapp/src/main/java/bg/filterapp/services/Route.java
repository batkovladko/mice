package bg.filterapp.services;

import static bg.filterapp.ApplicationContextListener.getConnectionString;
import static bg.filterapp.ApplicationContextListener.getPass;
import static bg.filterapp.ApplicationContextListener.getUsername;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class Route {
	public static EntityManager getEm() {
		Map<String, String> props = new HashMap<String, String>();
		props.put("javax.persistence.jdbc.url", getConnectionString());
		props.put("javax.persistence.jdbc.user", getUsername());
		props.put("javax.persistence.jdbc.password", getPass());
		
		return Persistence.createEntityManagerFactory("simple-service-webapp", props)
				.createEntityManager();	
	}
	
}
