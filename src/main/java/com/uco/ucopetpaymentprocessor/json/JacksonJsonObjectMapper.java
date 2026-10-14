package com.uco.ucopetpaymentprocessor.json;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Usa el JsonMapper (Jackson 3) que configura Spring Boot, que ya sabe
 * convertir LocalDateTime, BigDecimal, UUID y enums sin configuracion extra.
 */
@Component
public class JacksonJsonObjectMapper implements JsonObjectMapper {

    private final JsonMapper jsonMapper;

    public JacksonJsonObjectMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public <T> String toJson(T object) {
        return jsonMapper.writeValueAsString(object);
    }

    @Override
    public <T> T fromJson(String json, Class<T> targetClass) {
        return jsonMapper.readValue(json, targetClass);
    }
}