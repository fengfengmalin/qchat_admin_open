package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.qunar.qchat.admin.model.OnlineState;

import java.io.IOException;

/**
 * Author : mingxing.shao
 * Date : 15-10-19
 *
 */
public class OnlineStateJsonAdapter {
//    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineStateJsonAdapter.class);

    public static class Serializer extends JsonSerializer<OnlineState> {

        @Override
        public void serialize(OnlineState onlineState, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(onlineState.toString().toLowerCase());
        }
    }

    public static class Deserializer extends JsonDeserializer<OnlineState> {

        @Override
        public OnlineState deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            try {
                return OnlineState.valueOf(jsonParser.getText().toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }
}
