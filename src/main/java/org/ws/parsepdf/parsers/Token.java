package org.ws.parsepdf.parsers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charlie on 5/02/18.
 */
public class Token extends PDFTextStripper {

    private List<String> lines = null;
    private PDDocument document = null;
    /**
     * Instantiate a new PDFTextStripper object.
     *
     * @throws IOException If there is an error loading the properties.
     */
    public Token() throws IOException {

    }

    public Token(PDDocument document) throws IOException {
        this.lines = new ArrayList<String>();
        this.document = document;
        this.setSortByPosition(true);
        this.setStartPage(0);
        this.setEndPage(document.getNumberOfPages());
    }



    public String getToken(){
        String rtoken = null;
        Writer dummy = null;
        try {

            dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            writeText(document, dummy);
            rtoken = getLineToken();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                dummy.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rtoken;

    }

    public String getLineToken(){
        String result = "";
        for (String line : lines){
            result = line.length() > result.length() ? line : result;
        }
        return result;
    }



    /**
     * Override the default functionality of PDFTextStripper.writeString()
     */
    @Override
    protected void writeString(String str, List<TextPosition> textPositions) throws IOException {
        lines.add(str);
        // you may process the line here itself, as and when it is obtained
    }
}
