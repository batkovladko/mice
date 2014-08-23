package bg.mice.config;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.CommonProperties;

public class GsonProviderFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		 String postfix = '.' + context.getConfiguration().getRuntimeType().name().toLowerCase();

         context.property( CommonProperties.MOXY_JSON_FEATURE_DISABLE + postfix, true );
         context.register( GsonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class );

         return true;
	}

}
