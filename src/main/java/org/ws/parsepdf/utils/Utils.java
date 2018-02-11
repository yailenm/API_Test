package org.ws.parsepdf.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by charlie on 5/02/18.
 */
public class Utils {

    public static String toJson(Object object){
        String result = null;

        ObjectMapper mapper = new ObjectMapper();

        try {
            result = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }



}
