package bg.filterapp.services;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;

public class UTFInterceptor implements ContainerResponseFilter {
	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) {
		MediaType mt = response.getMediaType();
		if (mt != null && !mt.toString().contains("charset")) {
			response.getHeaders().putSingle("Content-Type", mt.toString() + ";charset=utf-8");
		}
	}
}