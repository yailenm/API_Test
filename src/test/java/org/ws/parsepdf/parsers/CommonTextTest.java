package parsepdf.parsers;

import jdk.nashorn.internal.objects.NativeArray;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.ws.parsepdf.parsers.CommonText;

import java.io.FileReader;
import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Pattern;

import java.io.File;
import java.io.IOException;

/**
 * Created by charlie on 28/03/18.
 */
public class CommonTextTest extends TestCase {

    public void testGetText() throws Exception {
        PDDocument document = null;

        File dir = new File("/home/charlie/Documents/Projects/citasmx/panama/PDFs");

        String[] pdfs = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches(".*pdf$",name);
            }
        });

        JSONParser parser = new JSONParser();

        Object obj = parser.parse(new FileReader("/home/charlie/workspace/appointment-embassy/citasmx/data/clients_pa.json"));

        JSONObject jsonObject = (JSONObject) obj;

        JSONArray clients = (JSONArray) jsonObject.get("clients");

        for (String pdf : pdfs) {
            try {
                document = PDDocument.load(new File(dir.getPath()+"/"+pdf));

                CommonText commonText = new CommonText(document);
                String pdfText = commonText.getText();

                Iterator<JSONObject> iterator = clients.iterator();
                boolean flag_stop = false;
                while (iterator.hasNext() && !flag_stop) {
                    JSONObject elem = iterator.next();
                    if(pdfText.indexOf((String)elem.get("passport")) > 0){
                        FileUtils.copyFile(new File(dir.getPath()+"/"+pdf), new File("/home/charlie/Desktop/mayo"+"/"+(String)elem.get("passport")+".pdf"));
                        System.out.println(pdfText+"File: " +pdf);
                        flag_stop =  true;

                    }
                }



                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }



    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}