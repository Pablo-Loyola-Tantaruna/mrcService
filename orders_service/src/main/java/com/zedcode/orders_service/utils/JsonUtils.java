package com.zedcode.orders_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object){
        try {
            return objectMapper.writeValueAsString(object);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> clszz){
        try {
            return objectMapper.readValue(json, clszz);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
