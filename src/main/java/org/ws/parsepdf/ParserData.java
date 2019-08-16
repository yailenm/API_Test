package org.ws.parsepdf;

/**
 * Created by charlie on 5/02/18.
 */
public class ParserData {

    private String captcha;
    private String ordercaptcha;
    private String token;

    public ParserData() {}

    public ParserData(String captcha, String token) {
        this.captcha = captcha;
        this.token = token;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
    public void setOrderCaptcha(String order) {
        this.ordercaptcha = order;
    }

    public String getOrderCaptcha() {
        return this.ordercaptcha;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
