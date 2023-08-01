package org.bigredbands.mb.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class PDFStringUtils {
    public static float stringWidth(String str, PDFont font,
        float fontSize) throws IOException
    {
        return font.getStringWidth(str) / 1000.0f * fontSize;
    }
}
