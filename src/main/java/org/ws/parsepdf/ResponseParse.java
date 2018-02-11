package org.ws.parsepdf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 * Created by charlie on 5/02/18.
 */
public class ResponseParse {

    private int status;
    private String msg;
    private ParserData data;

    public ResponseParse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ParserData getData() {
        return data;
    }

    public void setData(ParserData data) {
        this.data = data;
    }

    @JsonIgnore
    public String getJson()   {

        String result = null;

        ObjectMapper mapper = new ObjectMapper();

        try {
            result = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;

    }
}
