package parsepdf;

import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.ws.parsepdf.ParserData;
import org.ws.parsepdf.ResponseParse;
import org.ws.parsepdf.parsers.Captcha;

import java.io.File;
import java.io.IOException;

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
            document = PDDocument.load(new File("/home/charlie/Documents/Projects/appointments/mexico/DOC-20190622-WA0001.pdf"));
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

