package com.zhujun.spider.master.ui;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Json工具
 * 
 * @author zhujun
 * @date 2016年6月30日
 *
 */
public class JsonUtils {

	private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private static class MyModule extends SimpleModule {
		private static final long serialVersionUID = 2285547552631652785L;

		public MyModule() {
			super();
			
			// sql date
			addSerializer(java.sql.Date.class, new JsonSerializer<java.sql.Date>() {
				@Override
				public void serialize(java.sql.Date value, JsonGenerator gen, SerializerProvider serializers)
						throws IOException, JsonProcessingException {
					if (value != null) {
						gen.writeNumber(value.getTime());
					}
				}
			});
		}
		
	}
	
	static {
		OBJECT_MAPPER.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
					 .setSerializationInclusion(Include.NON_NULL);
		OBJECT_MAPPER.registerModule(new MyModule());
	}
	
	public static void writeValue(OutputStream out, Object value) throws JsonGenerationException, JsonMappingException, IOException {
		OBJECT_MAPPER.writeValue(out, value);
	}
	
}
