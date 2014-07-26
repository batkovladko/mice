package bg.filterapp.services;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class GsonHelper {

	private static Gson instance = initGson();

	private GsonHelper() {
	}

	public static Gson getGson() {
		return instance;
	}

	private static synchronized Gson initGson() {
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setExclusionStrategies(new GsonExclusionStrategy(JsonIgnore.class));
		gsonBuilder.addSerializationExclusionStrategy(new GsonExclusionStrategy(JsonIgnoreOnSerialization.class));
		gsonBuilder.registerTypeAdapterFactory(new TypeAdapterFactory() {
			@Override
			public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> tokenType) {
				final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, tokenType);
				return new TypeAdapter<T>() {
					@Override
					public void write(JsonWriter out, T value) throws IOException {
						delegate.write(out, value);
					}

					@SuppressWarnings("unchecked")
					@Override
					public T read(JsonReader reader) throws IOException {
						T t = delegate.read(reader);
						if(PostDeserializable.class.isAssignableFrom(tokenType.getRawType())) {
							PostDeserializable entity = (PostDeserializable) t;
							entity.postDeserialize();
							return (T) entity;
						}
						return t;
					}
				};
			}
		});

		gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				String date = json.getAsJsonPrimitive().getAsString();
				return new Date(Long.parseLong(date));
			}
		});
		gsonBuilder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(Long.toString(src.getTime()));
			}
		});
		return gsonBuilder.create();
	}

	private static class GsonExclusionStrategy implements ExclusionStrategy {
		private Class<? extends Annotation> annotationForIgnore;

		public GsonExclusionStrategy(Class<? extends Annotation> annotationForIgnore) {
			super();
			this.annotationForIgnore = annotationForIgnore;
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes field) {
			return field.getAnnotation(annotationForIgnore) != null;
		}
	}

}
