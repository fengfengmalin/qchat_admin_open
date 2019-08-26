package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;

import java.io.IOException;

/**
 * Author : mingxing.shao
 * Date : 15-10-26
 *
 */
public class SeatSortStrategyJsonAdapter {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SeatSortStrategyJsonAdapter.class);

    public static class Serializer extends JsonSerializer<SeatSortStrategyEnum> {

        @Override
        public void serialize(SeatSortStrategyEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Integer strategy = value.getStrategyId();

            if (strategy != null) {
                gen.writeNumber(strategy);
            } else {
                gen.writeNull();
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<SeatSortStrategyEnum> {

        @Override
        public SeatSortStrategyEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return SeatSortStrategyEnum.getStrategy(p.getIntValue());
        }
    }
}
