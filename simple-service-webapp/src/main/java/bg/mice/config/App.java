package bg.mice.config;

import org.glassfish.jersey.server.ResourceConfig;

public class App extends ResourceConfig {
	public App() {
		packages("bg.mice.services");
		register(UTFInterceptor.class);
		register(GsonProviderFeature.class);
	}
}