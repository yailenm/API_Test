package org.ws.parsepdf.parsers;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Created by charlie on 5/02/18.
 */
public class Captcha extends PDFStreamEngine {

    private String captcha;

    /**
     * Default constructor.
     *
     * @throws IOException If there is an error loading text stripper properties.
     */
    public Captcha() throws IOException {
    }

    public String getCaptchaBase64(PDDocument document) throws IOException {

        try {
            for (PDPage page : document.getPages()) {
                processPage(page);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return getCaptcha();

    }


    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if ("Do".equals(operation)) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) xobject;
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                if(imageHeight < 100){
                    // same image to local
                    BufferedImage bImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                    bImage = image.getImage();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(bImage, "PNG", Base64.getEncoder().wrap(os));

                    this.captcha = os.toString(StandardCharsets.ISO_8859_1.name());

                }



            } else if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                showForm(form);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    public String getCaptcha() {
        return captcha;
    }
}