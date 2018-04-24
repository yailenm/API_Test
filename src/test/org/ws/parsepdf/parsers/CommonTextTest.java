package org.ws.parsepdf.parsers;

import jdk.nashorn.internal.objects.NativeArray;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.util.regex.Pattern;

import java.io.File;
import java.io.IOException;

/**
 * Created by charlie on 28/03/18.
 */
public class CommonTextTest extends TestCase {

    public void testGetText() throws Exception {
        PDDocument document = null;

        File dir = new File("/home/charlie/Desktop/pdfs/");

        String[] pdfs = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches(".*pdf$",name);
            }
        });

        for (String pdf : pdfs) {
            try {
                document = PDDocument.load(new File(dir.getPath()+"/"+pdf));

                CommonText commonText = new CommonText(document);
                System.out.println(commonText.getText());
                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }



    }
}