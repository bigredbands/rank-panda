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

    public static String[] wordWrap(String stringToWrap,
        float widthLimit, float initialOffset,
        String wrapOn,
        PDFont font, float fontSize) throws IOException
    {
        ArrayList<String> strs = new ArrayList<String>();
        String[] tokens = stringToWrap.split(wrapOn);

        float remainingLine = widthLimit - initialOffset;
        String line = "";
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i < tokens.length - 1) token += wrapOn;

            float tokenSize = stringWidth(token, font, fontSize);

            // The token fits on the current line
            if (remainingLine >= tokenSize) {
                line += token;
                remainingLine -= tokenSize;
                continue;
            }

            // Token doesn't fit on the line
            if (line.isEmpty()) {
                // Just put the token on its own line (even though it's too long)
                strs.add(token.trim());
                continue;
            } else {
                // Token overflows this line - put it on the next one
                strs.add(line.trim());

                // Reset the next line
                remainingLine = widthLimit - tokenSize;
                line = token;
            }
        }

        if (!line.isEmpty()) {
            strs.add(line.trim());
        }

        return strs.toArray(new String[strs.size()]);
    }
}
