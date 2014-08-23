package bg.mice.config;

import org.glassfish.jersey.server.ResourceConfig;

public class App extends ResourceConfig {
	public static final String MICE_SERVICES_PKG = "bg.mice.services";
	public static final int MAX_FILE_SIZE = 1024 * 1024 * 2;

	public App() {
		packages(MICE_SERVICES_PKG);
		register(UTFInterceptor.class);
		register(GsonProviderFeature.class);
	}
}