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
 * Created by charlie on 28/03/18.
 */
public class CommonText extends PDFTextStripper {

    private PDDocument document = null;
    private List<String> lines = null;

    public CommonText() throws IOException {
    }

    public CommonText(PDDocument document) throws IOException{
        this.document = document;
        this.lines = new ArrayList<String>();
        this.setSortByPosition(true);
        this.setStartPage(0);
        this.setEndPage(document.getNumberOfPages());
    }

    public String getText(){

        lines = getLines();
        return lines.toString();
    }

    public List<String> getLines() {

        if (lines.size() > 0 ){
            return lines;
        }

        Writer dummy = null;
        try {

            dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            writeText(document, dummy);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                dummy.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lines;
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        lines.add(text);
    }
}
