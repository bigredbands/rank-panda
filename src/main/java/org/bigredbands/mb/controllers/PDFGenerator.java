package org.bigredbands.mb.controllers;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.DrillInfo;
import org.bigredbands.mb.models.Field;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.utils.PDFStringUtils;
import org.bigredbands.mb.views.PdfImage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;


/**
 *
 * Used to generate PDFs from marching band routine.
 *
 */
public class PDFGenerator {

    /**
     * Default constructor
     */
    public PDFGenerator() {
    }

    private PDPageContentStream addNewPage(PDDocument document, PDRectangle mediaBox) throws IOException {
        // Create a new blank page and add it to the document
        PDPage blankPage = new PDPage(mediaBox);

        // Rotate page for landscape mode
        blankPage.setRotation(90);

        // add page to the document
        document.addPage(blankPage);

        // Initialize ContentStream to add content to PDF page
        PDPageContentStream contentStream = new PDPageContentStream(
            document, blankPage, PDPageContentStream.AppendMode.OVERWRITE,
            false);

        // add the rotation using the current transformation matrix including a
        // translation of media box width to use the lower left corner as 0,0
        // reference; properly orients text and image
        contentStream.transform(new Matrix(0, 1, -1, 0, mediaBox.getWidth(), 0));
        return contentStream;
    }

    /**
     * Used to generate the pdf files
     *
     * @param drillInfo
     *            - contains information about the drill, including song name,
     *            ranks, moves, commands and comments
     * @param file
     *            - file to which the PDF will be saved
     * @throws IOException
     */
    public void createPDF(DrillInfo drillInfo, File file) throws IOException {

        // Create a new empty document
        PDDocument document = new PDDocument();

        // Field dimensions
        // Units are in feet
        Field field = Field.CollegeFootball; // TODO: allow setting this dynamically

        // Page dimensions
        // Units are in 1/72 of an inch ("user-space units")
        PDRectangle pageSize = PDRectangle.LETTER;
        float pageHeight = pageSize.getWidth(); // height of page
                                                // (landscape)
        float pageWidth = pageSize.getHeight(); // width of page (landscape)
        float pageMarginX = 0.75f * 72f; // 3/4" margin X (72 units/inch)
        float pageMarginY = 0.5f * 72f; // 1/2" margin Y (72 units/inch)

        float drillWidth = pageWidth - 2 * pageMarginX;
        float drillHeight = drillWidth / (field.TotalLength / field.Height);
        float textMarginX = pageMarginX + (field.EndzoneWidth / field.TotalLength) * drillWidth;

        // Fonts
        PDFont pdfFont = PDType1Font.HELVETICA;
        float commentFontSize = 12.0f;
        float fontSize = 10.0f;
        float lineSpacing = 1.5f;

        // Drill image dimensions
        // Units are in pixels
        float imageWidth = drillWidth;
        float imageHeight = drillHeight;
        Dimension dim = new Dimension((int) imageWidth, (int) imageHeight);

        // Conversion factor
        float ftToPx = imageWidth / field.TotalLength;

        // Initialize iterated variables
        int pageNumber = 1; // page number
        int moveNumber = 0; // move number
        int begMeasure = 0; // beginning measure of move
        int currentMeasure = 0; // the measure that is currently being counted
        int currentCountsPerMeasure = 4; // functions as the time signature
        int extraCounts = 0; // any extra counts from calculating the end measure to be carried on to the next move

        // iterating through each move in the drill
        for (Move move : drillInfo.getMoves()) {

            // Create the first page of the move
            PDPageContentStream contentStream = addNewPage(document, pageSize);
            contentStream.setFont(pdfFont, fontSize);

            // Get the header text
            String drillTitle, measureText, moveLabel;

            drillTitle = drillInfo.getSongName();

            // we have a map of measure number to count per measure
            // currently assumes 4 counts per measure
            int totalCounts = extraCounts;
            while (move.getCounts() > totalCounts){
                currentCountsPerMeasure = drillInfo.getCountsHashMap().getOrDefault(currentMeasure + 1, currentCountsPerMeasure);
                totalCounts += currentCountsPerMeasure;
                currentMeasure ++;
            }

            measureText = "Measures:  " + begMeasure + " - " + currentMeasure;

            if (totalCounts > move.getCounts()) {
                begMeasure = currentMeasure;
                extraCounts = totalCounts - move.getCounts();
            } else {
                extraCounts = 0;
                begMeasure = currentMeasure + 1;
            }

            moveLabel = "Move " + moveNumber;

            // Print the header
            float yPosition = pageHeight - pageMarginY;
            contentStream.beginText();
            contentStream.newLineAtOffset(textMarginX, yPosition - fontSize);

            // Print drill title
            contentStream.showText(drillTitle);

            // Print measures
            float measureTextWidth = PDFStringUtils.stringWidth(measureText, pdfFont, fontSize);
            contentStream.newLineAtOffset((pageWidth - measureTextWidth) / 2 - textMarginX, 0);
            contentStream.showText(measureText);

            // Print move label
            float moveLabelWidth = PDFStringUtils.stringWidth(moveLabel, pdfFont, fontSize);
            contentStream.newLineAtOffset((measureTextWidth + pageWidth) / 2 - textMarginX - moveLabelWidth, 0);
            contentStream.showText(moveLabel);
            contentStream.endText();

            // Move the current y position past the header
            yPosition -= lineSpacing * fontSize;

            // add image to PDF
            PdfImage image = new PdfImage(ftToPx * 3.0f, dim, move.getEndPositions());
            image.setPreferredSize(dim);
            image.setSize(dim);
            BufferedImage bi = createImage(image);
            PDImageXObject img = LosslessFactory.createFromImage(document, bi);

            contentStream.drawImage(img,
                pageMarginX, yPosition - drillHeight,
                drillWidth, drillHeight);
            yPosition -= drillHeight;

            // print instructions for ranks
            // if rank commands are the same of a prior rank add that rank to
            // one already there
            // otherwise start text in next column or if no more space on the
            // right, next row
            if (moveNumber > 0) {
                ArrayList<String> rankNames = new ArrayList<String>(); // all rank names

                HashMap<ArrayList<CommandPair>, String> uniqueMoves = new HashMap<ArrayList<CommandPair>, String>();

                for (Entry<String, ArrayList<CommandPair>> entry : move
                        .getCommands().entrySet()) {

                    boolean match = false; // match indicates whether there is
                                            // already this unique set of
                                            // commands in the hashmap
                    String rankName = entry.getKey();
                    rankNames.add(rankName);
                    ArrayList<CommandPair> comPairs = entry.getValue();

                    // for every key in uniquemoves
                    for (Entry<ArrayList<CommandPair>, String> umEntry : uniqueMoves
                            .entrySet()) {

                        // if this comPairs = something already in the keyset
                        if (comPairs.equals(umEntry.getKey())) {
                            // add ", rankname" to value
                            uniqueMoves.put(umEntry.getKey(),
                                    umEntry.getValue() + ", " + rankName);
                            match = true;
                            break;
                        }
                    }
                    // if this comPairs not already in the keyset
                    if (match == false) {
                        uniqueMoves.put(comPairs, rankName);
                    }

                }

                // print move's comments
                if (move.getComments().length() > 0) {
                    contentStream.setFont(pdfFont, commentFontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(textMarginX, pageHeight - 385);
                    contentStream.showText("Comments:  " + move.getComments());
                    contentStream.endText();
                    contentStream.setFont(pdfFont, fontSize);
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(textMarginX, pageHeight - 410);

                int columnCount = 0; // tracks number of columns used

                // find longest command - determines number of lines before next
                // instruction
                int maxLength = 0; // keeps track of largest unique instruction
                                    // (in terms of size of string)

                for (Entry<ArrayList<CommandPair>, String> umEntry : uniqueMoves
                        .entrySet()) {
                    String instructions = umEntry.getValue()
                            + ": "
                            + umEntry
                                    .getKey()
                                    .toString()
                                    .substring(
                                            1,
                                            umEntry.getKey().toString()
                                                    .length() - 1);
                    if (instructions.length() > maxLength) {
                        maxLength = instructions.length();
                    }
                }

                int addLines = maxLength / 35; // additional lines needed because of longer instructions


                for (Entry<ArrayList<CommandPair>, String> umEntry2 : uniqueMoves
                        .entrySet()) {
                    String instructions2 = umEntry2.getValue()
                            + ": "
                            + umEntry2
                                    .getKey()
                                    .toString()
                                    .substring(
                                            1,
                                            umEntry2.getKey().toString()
                                                    .length() - 1);

                    // if all instructions less than 30 characters
                    if (addLines == 0) {

                        if (columnCount % 3 == 0) {
                            contentStream.showText(instructions2);
                            contentStream.newLineAtOffset(190, 0);
                        } else if (columnCount % 3 == 1) {
                            contentStream.showText(instructions2);
                            contentStream.newLineAtOffset(190, 0);
                        } else if (columnCount % 3 == 2) {
                            contentStream.showText(instructions2);
                            contentStream.newLineAtOffset(-380, -30);
                        }
                        columnCount++;
                    } else {
                        // if at least one instruction is greater than 30 characters
                        // create an ArrayList of substrings
                        ArrayList<String> divInstructions = new ArrayList<String>(); // contains instructions for each line
                        int i = 0;
                        String subString = "";

                        // if instruction length is less than 30, just add the
                        // entire instruction
                        if (instructions2.length() < 30) {
                            subString = instructions2.substring(i,
                                    instructions2.length());
                            divInstructions.add(subString);
                        }

                        // else you need to print instruction on multiple lines
                        else {
                            int instrLength = 1; // subinstruction length
                            while (i < instructions2.length()) {

                                instrLength = Math.min(35, instructions2
                                        .substring(i, instructions2.length())
                                        .length());// the min of 35 or amount of
                                                    // characters left in
                                                    // instruction
                                subString = instructions2.substring(i, i
                                        + instrLength); // !!!!!!!!!need to fix
                                                        // to go to comma w/e
                                divInstructions.add(subString);
                                i = i + 35;
                            }
                        }

                        int divInstSize = divInstructions.size(); // number of
                                                                    // subinstructions
                        int numLeft = divInstructions.size(); // tracks number of subinstructions left to print

                        if (divInstSize == 1) {
                            if (columnCount % 3 == 0) {
                                contentStream.showText(instructions2);
                                contentStream.newLineAtOffset(190, 0);
                            } else if (columnCount % 3 == 1) {
                                contentStream.showText(instructions2);
                                contentStream.newLineAtOffset(190, 0);
                            } else if (columnCount % 3 == 2) {
                                contentStream.showText(instructions2);
                                contentStream.newLineAtOffset(-380,
                                        -15 + addLines * (-15));
                            }
                            columnCount++;
                        }

                        else if (divInstSize > 1) {
                            if (columnCount % 3 == 0) {
                                for (String s : divInstructions) {
                                    if (numLeft > 0) {
                                        contentStream.showText(s);
                                        contentStream.newLineAtOffset(
                                                0, -15);
                                        numLeft--;
                                    }
                                }
                                contentStream.newLineAtOffset(190,
                                        (divInstSize) * 15);
                            }

                            else if (columnCount % 3 == 1) {
                                for (String s : divInstructions) {
                                    if (numLeft > 0) {
                                        contentStream.showText(s);
                                        contentStream.newLineAtOffset(
                                                0, -15);
                                        numLeft--;
                                    }
                                }
                                contentStream.newLineAtOffset(190,
                                        (divInstSize) * 15);
                            }

                            else if (columnCount % 3 == 2) {
                                for (String s : divInstructions) {
                                    if (numLeft > 0) {
                                        contentStream.showText(s);
                                        contentStream.newLineAtOffset(
                                                0, -15);
                                        numLeft--;
                                    }
                                }
                                contentStream.newLineAtOffset(-380,
                                        (addLines - 1) * (-15));
                            }
                            columnCount++;
                        }
                    }

                }
                contentStream.endText();

            }

            // print page number
            /*if (moveNumber >= 0) {
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth / 2) - 15,
                        bufferBottom - 20);
                contentStream.showText("Page " + pageNumber);
                contentStream.endText();
            }*/

            pageNumber++; // increment page number
            moveNumber++; // increment move number

            // close out content stream
            contentStream.close();
        }

        // Save the newly created document
        document.save(file.getAbsolutePath());

        // properly close the document
        document.close();

    }

    /**
     * converts panel to BufferedImage
     *
     * @param panel
     *            - JPanel of football field
     * @return - returns BufferedImage of football field with ranks
     */
    public BufferedImage createImage(JPanel panel) {

        BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        panel.paint(g);
        return bi;
    }

}
