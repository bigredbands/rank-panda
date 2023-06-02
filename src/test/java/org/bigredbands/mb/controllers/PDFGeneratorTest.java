package org.bigredbands.mb.controllers;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.bigredbands.mb.controllers.PDFGenerator;
import org.bigredbands.mb.exceptions.DrillXMLException;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.DrillInfo;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;
import org.junit.Test;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;
import org.bigredbands.mb.views.PdfImage;

public class PDFGeneratorTest {

    private static final String outputPath = "out/test/PDFGeneratorTest/";

    @BeforeClass
    public static void createOutputDir() {
        // Create the output directory if it doesn't already exist
        new File(outputPath).mkdirs();
    }

    @Test
    public void testCreateBlankPDF() throws ParserConfigurationException,
            SAXException, IOException, DrillXMLException, COSVisitorException {
        // create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();

        // set the expected song constants
        expectedDrillInfo.getTempoHashMap().put(1, 120);
        expectedDrillInfo.getTempoHashMap().put(35, 160);
        expectedDrillInfo.getCountsHashMap().put(1, 4);
        expectedDrillInfo.getCountsHashMap().put(20, 2);
        //set title
        expectedDrillInfo.setSongName("Drill #1");

        // create the expected moves
        // move 0
        Move move = new Move();
        HashMap<String, RankPosition> startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(150, 80), new Point(
                50, 100)));
        startPositions.put("B", new RankPosition(new Point(200, 80), new Point(
                100, 100)));


        HashMap<String, ArrayList<CommandPair>> rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        rankCommands.put("A", new ArrayList<CommandPair>());
        rankCommands.put("B", new ArrayList<CommandPair>());


        HashMap<String, RankPosition> endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(20, 30), new Point(70,
                10)));
        endPositions.put("B", new RankPosition(new Point(70, 50), new Point(50,
                35)));

        move.setStartPositions(startPositions);
        move.setCommands(rankCommands);
        move.setEndPositions(endPositions);

        move.setComments("This is the zero move");

        expectedDrillInfo.getMoves().add(move);

        // move 2
        move = new Move();
        startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(20, 30), new Point(70,
                10)));
        startPositions.put("B", new RankPosition(new Point(70, 50), new Point(50,
                35)));

        rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        ArrayList<CommandPair> commands = new ArrayList<CommandPair>();
        commands.add(new CommandPair(CommandPair.MT, 12));
        commands.add(new CommandPair(CommandPair.FM, 8));
        commands.add(new CommandPair(CommandPair.HALT, 4));
        commands.add(new CommandPair(CommandPair.RS, 4));
        commands.add(new CommandPair(CommandPair.LS, 4));
        commands.add(new CommandPair(CommandPair.FM, 4));
        rankCommands.put("A", commands);

        ArrayList<CommandPair> commands2 = new ArrayList<CommandPair>();
        commands2.add(new CommandPair(CommandPair.MT, 12));
        commands2.add(new CommandPair(CommandPair.FM, 8));
        commands2.add(new CommandPair(CommandPair.HALT, 5));
        commands2.add(new CommandPair(CommandPair.FM, 8));
        commands2.add(new CommandPair(CommandPair.HALT, 5));

        rankCommands.put("B", commands2);

        ArrayList<CommandPair> commands3 = new ArrayList<CommandPair>();
        commands3.add(new CommandPair(CommandPair.MT, 12));
        commands3.add(new CommandPair(CommandPair.FM, 8));
        commands3.add(new CommandPair(CommandPair.HALT, 6));
        commands3.add(new CommandPair(CommandPair.MT, 12));
        commands3.add(new CommandPair(CommandPair.FM, 8));
        commands3.add(new CommandPair(CommandPair.HALT, 6));
        commands3.add(new CommandPair(CommandPair.MT, 12));
        commands3.add(new CommandPair(CommandPair.FM, 8));

        rankCommands.put("C", commands3);

        ArrayList<CommandPair> commands4 = new ArrayList<CommandPair>();
        commands4.add(new CommandPair(CommandPair.MT, 12));
        commands4.add(new CommandPair(CommandPair.FM, 8));
        commands4.add(new CommandPair(CommandPair.HALT, 7));
        commands4.add(new CommandPair(CommandPair.MT, 12));
        commands4.add(new CommandPair(CommandPair.FM, 8));
        commands4.add(new CommandPair(CommandPair.HALT, 50));
        commands4.add(new CommandPair(CommandPair.MT, 12));
        commands4.add(new CommandPair(CommandPair.HALT, 7));
        commands4.add(new CommandPair(CommandPair.MT, 12));

        rankCommands.put("D", commands4);

        ArrayList<CommandPair> commands5 = new ArrayList<CommandPair>();
        commands5.add(new CommandPair(CommandPair.MT, 12));
        commands5.add(new CommandPair(CommandPair.FM, 8));
        commands5.add(new CommandPair(CommandPair.HALT, 7));
        commands5.add(new CommandPair(CommandPair.MT, 12));
        commands5.add(new CommandPair(CommandPair.FM, 8));
        commands5.add(new CommandPair(CommandPair.HALT, 50));
        commands5.add(new CommandPair(CommandPair.MT, 12));
        commands5.add(new CommandPair(CommandPair.HALT, 7));
        commands5.add(new CommandPair(CommandPair.MT, 12));

        rankCommands.put("E", commands5);

        ArrayList<CommandPair> commands6 = new ArrayList<CommandPair>();
        commands6.add(new CommandPair(CommandPair.MT, 12));
        commands6.add(new CommandPair(CommandPair.FM, 8));
        commands6.add(new CommandPair(CommandPair.HALT, 7));
        commands6.add(new CommandPair(CommandPair.MT, 12));
        commands6.add(new CommandPair(CommandPair.FM, 8));
        commands6.add(new CommandPair(CommandPair.HALT, 50));
        commands6.add(new CommandPair(CommandPair.MT, 12));
        commands6.add(new CommandPair(CommandPair.HALT, 7));
        commands6.add(new CommandPair(CommandPair.MT, 12));

        rankCommands.put("FASF", commands6);


        endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(10, 30), new Point(60,
                10)));
        endPositions.put("B", new RankPosition(new Point(70, 40), new Point(50,
                25)));

        move.setStartPositions(startPositions);
        move.setCommands(rankCommands);
        move.setEndPositions(endPositions);

        move.setComments("This is the first move");

        expectedDrillInfo.getMoves().add(move);

        testPDFGenerator(expectedDrillInfo, new File(outputPath
                + "2-routine.pdf"));
    }

    private void testPDFGenerator(DrillInfo expectedDrillInfo, File outputFile)
            throws ParserConfigurationException, SAXException, IOException,
            DrillXMLException, COSVisitorException {
        // generate the pdf
        PDFGenerator generator = new PDFGenerator();
        generator.createPDF(expectedDrillInfo, outputFile);

    }
}
