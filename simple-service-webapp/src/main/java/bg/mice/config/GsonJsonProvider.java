package bg.mice.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import bg.mice.helpers.GsonHelper;

import com.google.gson.JsonParseException;

/**
 * GSON provider for jax-rs
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GsonJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

	@Override
	public long getSize(Object object, Class<?> clazz, Type type, Annotation[] annotation, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annontation, MediaType mediaType) {
		return true;
	}

	@Override
	public void writeTo(Object object, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream content) throws IOException,
			WebApplicationException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(content, "UTF-8")); //$NON-NLS-1$
		try {
			GsonHelper.getGson().toJson(object, type, writer);
		} finally {
			writer.close();
		}
	}

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotation, MediaType mediaType) {
		return true;
	}

	@Override
	public Object readFrom(Class<Object> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream content) throws IOException,
			WebApplicationException {
		Reader reader = new BufferedReader(new InputStreamReader(content, "UTF-8")); //$NON-NLS-1$
		try {
			return GsonHelper.getGson().fromJson(reader, type);
		} catch (JsonParseException e) {
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		} finally {
			reader.close();
		}
	}

}
