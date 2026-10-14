package com.uco.ucopetpaymentprocessor.json;

public interface JsonObjectMapper {

    <T> String toJson(T object);

    <T> T fromJson(String json, Class<T> targetClass);
}