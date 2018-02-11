package org.ws.parsepdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.ws.parsepdf.parsers.Captcha;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by charlie on 5/02/18.
 */
public class ResponseParseTest extends TestCase {

    public void testGetJson() throws Exception {

        File file = File.createTempFile("codigo-",".pdf");
        System.out.println(file.getAbsolutePath());

        ResponseParse responseParse = new ResponseParse(200,"File parsed");
        ParserData data = new ParserData();

        PDDocument document = null;

        try {
            document = PDDocument.load(new File("/tmp/codigo-3255602287165515874.pdf"));
            Captcha captcha = new Captcha();

            data.setCaptcha(captcha.getCaptchaBase64(document));
            org.ws.parsepdf.parsers.Token token = new org.ws.parsepdf.parsers.Token(document);
            data.setToken(token.getToken());
            document.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        responseParse.setData(data);

        System.out.println(responseParse.getJson());


    }
}

