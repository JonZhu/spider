package com.zhujun.spider.master.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.Date;

/**
 * JSON序列化配置
 *
 * @author zhujun
 * @desc JsonSerializerConfig
 * @time 2018/3/28 10:50
 */
@JsonComponent
public class JsonSerializerConfig {
    public static class DateSerializer extends JsonSerializer<Date> {

        /**
         * 时间序列化为毫秒
         *
         * @param date
         * @param jsonGenerator
         * @param serializerProvider
         * @throws IOException
         */
        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(date.getTime());
        }
    }
}
