package bg.filterapp.services;

import org.glassfish.jersey.server.ResourceConfig;

public class App extends ResourceConfig {
	public App() {
		packages("bg.filterapp.services");
		register(UTFInterceptor.class);
		register(GsonProviderFeature.class);
	}
}