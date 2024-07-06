package org.bigredbands.mb.controllers;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.bigredbands.mb.models.ConsolidatedDrillInstruction;
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

    private PDPageContentStream addNewPage(PDDocument document, PDRectangle mediaBox,
            int pageNumber, float margin, PDFont font, float fontSize) throws IOException {
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

        // Print the page number
        contentStream.setFont(font, fontSize);
        String pageNumString = Integer.toString(pageNumber);
        contentStream.beginText();
        contentStream.newLineAtOffset((mediaBox.getHeight() - font.getStringWidth(pageNumString) / 1000.0f) / 2,
                margin);
        contentStream.showText(pageNumString);
        contentStream.endText();

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
        float drillHeight = drillWidth / (field.TotalLength / field.TotalHeight);
        float textMarginX = pageMarginX + (field.EndzoneWidth / field.TotalLength) * drillWidth;

        // Fonts
        PDFont pdfFont = PDType1Font.HELVETICA;
        PDFont rankFont = PDType1Font.HELVETICA_BOLD;
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
            PDPageContentStream contentStream = addNewPage(document, pageSize,
                pageNumber++, pageMarginY, pdfFont, fontSize);
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
                // print move's comments
                if (move.getComments().length() > 0) {
                    contentStream.setFont(pdfFont, commentFontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(textMarginX, yPosition - commentFontSize);
                    contentStream.showText("Comments:  " + move.getComments());
                    contentStream.endText();

                    // Reset to original font size
                    contentStream.setFont(pdfFont, fontSize);

                    // Offset for the comment space
                    yPosition -= lineSpacing * commentFontSize;
                }

                // Divide per-rank commands into columns
                int numColumns = 3;
                float columnWidth = (pageWidth - 2 * textMarginX) / (float) numColumns;
                float commandMaxWidth = columnWidth * 0.95f;

                // Get the ranks grouped by unique move
                List<ConsolidatedDrillInstruction> rankGroups = move.getCommands().entrySet()
                    // First, convert to map of <list of ranks> -> <list of commands>
                    .stream().collect(
                        Collectors.groupingBy(
                            // TODO: we should group by the string generated by
                            // the list of commands, NOT the values of the
                            // CommandPairs. Two direct-to-point CommandPairs
                            // with different destinations aren't considered
                            // "equal", but will render identically in PDF
                            // instructions.
                            Map.Entry::getValue,
                            Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                        )
                    ).entrySet()
                    // Then, convert the <list of ranks> -> <list of command> entries to instruction objects
                    .stream().map(e ->
                        new ConsolidatedDrillInstruction(e.getValue(), rankFont, fontSize,
                                                         e.getKey(), pdfFont, fontSize,
                                                         commandMaxWidth)
                    ).sorted(
                        Comparator.comparingInt(ConsolidatedDrillInstruction::getNumLines).reversed()
                    ).collect(Collectors.toList());

                contentStream.beginText();
                contentStream.setLeading(lineSpacing * fontSize);
                contentStream.newLineAtOffset(textMarginX, yPosition - fontSize);

                int rankGroupCounter = 0;
                int maxLines = 1;
                boolean newPage = false;
                for (ConsolidatedDrillInstruction rankGroup : rankGroups) {
                    int totalLines = rankGroup.getNumLines();
                    int remainingLines = (int)((yPosition - pageMarginY) / (lineSpacing * fontSize));
                    if (!newPage && remainingLines < totalLines) {
                        // End the current page
                        contentStream.endText();
                        contentStream.close();

                        // Add a new page
                        yPosition = pageHeight - pageMarginY;
                        contentStream = addNewPage(document, pageSize,
                            pageNumber++, pageMarginY, pdfFont, fontSize);
                        contentStream.beginText();
                        contentStream.setLeading(lineSpacing * fontSize);
                        contentStream.newLineAtOffset(textMarginX, yPosition - fontSize);
                        newPage = true;
                    }

                    contentStream.setFont(rankFont, fontSize);
                    for (String line : rankGroup.getRankLines()) {
                        contentStream.showText(line);
                        contentStream.newLine();
                    }

                    // Since we need to print the next text on the same line,
                    // back up a line and indent by the appropriate offset.
                    float commandStrOffset = rankGroup.getCommandOffset();
                    contentStream.newLineAtOffset(commandStrOffset, lineSpacing * fontSize);
                    contentStream.setFont(pdfFont, fontSize);
                    for (String line : rankGroup.getCommandLines()) {
                        contentStream.showText(line);
                        contentStream.newLine();
                        contentStream.newLineAtOffset(-commandStrOffset, 0);

                        // Zero out the offset so we only apply it to the first
                        // line.
                        commandStrOffset = 0;
                    }

                    contentStream.newLineAtOffset(0, totalLines * lineSpacing * fontSize);
                    maxLines = Math.max(maxLines, totalLines);

                    // Move to the next writeout position
                    rankGroupCounter++;
                    if (rankGroupCounter % numColumns == 0) {
                        float nextRowOffset = (maxLines + 1) * lineSpacing * fontSize;
                        contentStream.newLineAtOffset(-((numColumns - 1) * columnWidth), -nextRowOffset);
                        yPosition -= nextRowOffset;
                        newPage = false;
                        maxLines = 1;
                    } else {
                        contentStream.newLineAtOffset(columnWidth, 0);
                    }

                }
                contentStream.endText();

            }

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
