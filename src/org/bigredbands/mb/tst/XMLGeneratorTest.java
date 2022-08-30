package org.bigredbands.mb.tst;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.bigredbands.mb.controllers.XMLGenerator;
import org.bigredbands.mb.controllers.XMLParser;
import org.bigredbands.mb.exceptions.DrillXMLException;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.DrillInfo;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;
import org.junit.Test;
import org.xml.sax.SAXException;

public class XMLGeneratorTest {
    
    private final String outputPath = "tst-data/XMLGenerator/output/";
    
    @Test
    public void testOneTempoChange() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        expectedDrillInfo.getTempoHashMap().put(10, 180);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "1-one-tempo-change-output.pnd"));
    }
    
    @Test
    public void testMultipleTempoChanges() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        expectedDrillInfo.getTempoHashMap().put(10, 180);
        expectedDrillInfo.getTempoHashMap().put(20, 190);
        expectedDrillInfo.getTempoHashMap().put(30, 200);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "2-multiple-tempo-change-output.pnd"));
    }
    
    @Test
    public void testOneCountChange() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        expectedDrillInfo.getCountsHashMap().put(10, 4);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "3-one-counts-change-output.pnd"));
    }
    
    @Test
    public void testMultipleCountChanges() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        expectedDrillInfo.getCountsHashMap().put(10, 4);
        expectedDrillInfo.getCountsHashMap().put(20, 5);
        expectedDrillInfo.getCountsHashMap().put(30, 6);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "4-multiple-counts-change-output.pnd"));
    }
    
    @Test
    public void testEmptyProject() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //test the generator output with the parser
        testGeneratorAndParser(new DrillInfo(), new File(outputPath + "5-empty-project-output.pnd"));
    }
    
    @Test
    public void testOneMoveNoRanks() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        expectedDrillInfo.getMoves().add(new Move());
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "6-one-move-no-ranks-output.pnd"));
    }
    
    @Test
    public void testOneMoveNoRanksWithComments() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        Move move = new Move();
        move.setComments("There are no ranks here!");
        expectedDrillInfo.getMoves().add(move);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "7-one-move-no-ranks-with-comments-output.pnd"));
    }
    
    @Test
    public void testTwoMovesOneRankSongConstants() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        
        //set the expected song constants
        expectedDrillInfo.getTempoHashMap().put(1, 120);
        expectedDrillInfo.getTempoHashMap().put(35, 160);
        expectedDrillInfo.getCountsHashMap().put(1, 4);
        expectedDrillInfo.getCountsHashMap().put(20, 2);
        
        //create the expected moves
        //move 0
        Move move = new Move();
        HashMap<String, RankPosition> startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        move.setStartPositions(startPositions);
        
        HashMap<String, ArrayList<CommandPair>> rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        rankCommands.put("A", new ArrayList<CommandPair>());
        move.setCommands(rankCommands);
        
        HashMap<String, RankPosition> endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        move.setEndPositions(endPositions);
        
        move.setComments("This is the zero move");
        
        expectedDrillInfo.getMoves().add(move);
        
        //move 2
        move = new Move();
        startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        move.setStartPositions(startPositions);
        
        rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        ArrayList<CommandPair> commands = new ArrayList<CommandPair>();
        commands.add(new CommandPair(CommandPair.MT, 12));
        commands.add(new CommandPair(CommandPair.FM, 8));
        commands.add(new CommandPair(CommandPair.HALT, 4));
        rankCommands.put("A", commands);
        move.setCommands(rankCommands);
        
        endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(45, 15), new Point(55, 15)));
        move.setEndPositions(endPositions);
        
        move.setComments("This is the first move");
        
        expectedDrillInfo.getMoves().add(move);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "8-two-moves-one-rank-song-constants-output.pnd"));
    }
    
    @Test
    public void testTwoMovesMultipleRankSongConstants() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
DrillInfo expectedDrillInfo = new DrillInfo();
        
        //set the expected song constants
        expectedDrillInfo.getTempoHashMap().put(1, 120);
        expectedDrillInfo.getTempoHashMap().put(35, 160);
        expectedDrillInfo.getCountsHashMap().put(1, 4);
        expectedDrillInfo.getCountsHashMap().put(20, 2);
        
        //create the expected moves
        //move 0
        Move move = new Move();
        HashMap<String, RankPosition> startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        startPositions.put("B", new RankPosition(new Point(45, 20), new Point(55, 20)));
        move.setStartPositions(startPositions);
        
        HashMap<String, ArrayList<CommandPair>> rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        rankCommands.put("A", new ArrayList<CommandPair>());
        rankCommands.put("B", new ArrayList<CommandPair>());
        move.setCommands(rankCommands);
        
        HashMap<String, RankPosition> endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        endPositions.put("B", new RankPosition(new Point(45, 20), new Point(55, 20)));
        move.setEndPositions(endPositions);
        
        move.setComments("This is the zero move");
        
        expectedDrillInfo.getMoves().add(move);
        
        //move 2
        move = new Move();
        startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        startPositions.put("B", new RankPosition(new Point(45, 20), new Point(55, 20)));
        move.setStartPositions(startPositions);
        
        rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        ArrayList<CommandPair> commands = new ArrayList<CommandPair>();
        commands.add(new CommandPair(CommandPair.MT, 12));
        commands.add(new CommandPair(CommandPair.FM, 8));
        commands.add(new CommandPair(CommandPair.HALT, 4));
        rankCommands.put("A", commands);
        rankCommands.put("B", commands);
        move.setCommands(rankCommands);
        
        endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(45, 15), new Point(55, 15)));
        endPositions.put("B", new RankPosition(new Point(45, 25), new Point(55, 25)));
        move.setEndPositions(endPositions);
        
        move.setComments("This is the first move");
        
        expectedDrillInfo.getMoves().add(move);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "9-two-moves-two-ranks-song-constants-output.pnd"));
        
    }
    
    @Test
    public void testTwoMovesOneRankSongConstantsRenamedCommand() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //create the expected DrillInfo
        DrillInfo expectedDrillInfo = new DrillInfo();
        
        //set the expected song constants
        expectedDrillInfo.getTempoHashMap().put(1, 120);
        expectedDrillInfo.getTempoHashMap().put(35, 160);
        expectedDrillInfo.getCountsHashMap().put(1, 4);
        expectedDrillInfo.getCountsHashMap().put(20, 2);
        
        //create the expected moves
        //move 0
        Move move = new Move();
        HashMap<String, RankPosition> startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        move.setStartPositions(startPositions);
        
        HashMap<String, ArrayList<CommandPair>> rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        rankCommands.put("A", new ArrayList<CommandPair>());
        move.setCommands(rankCommands);
        
        HashMap<String, RankPosition> endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        move.setEndPositions(endPositions);
        
        move.setComments("This is the zero move");
        
        expectedDrillInfo.getMoves().add(move);
        
        //move 2
        move = new Move();
        startPositions = new HashMap<String, RankPosition>();
        startPositions.put("A", new RankPosition(new Point(45, 10), new Point(55, 10)));
        move.setStartPositions(startPositions);
        
        rankCommands = new HashMap<String, ArrayList<CommandPair>>();
        ArrayList<CommandPair> commands = new ArrayList<CommandPair>();
        commands.add(new CommandPair(CommandPair.MT, 12));
        commands.add(new CommandPair(CommandPair.FM, 8));
        commands.add(new CommandPair(CommandPair.HALT, 4, "High step"));
        rankCommands.put("A", commands);
        move.setCommands(rankCommands);
        
        endPositions = new HashMap<String, RankPosition>();
        endPositions.put("A", new RankPosition(new Point(45, 15), new Point(55, 15)));
        move.setEndPositions(endPositions);
        
        move.setComments("This is the first move");
        
        expectedDrillInfo.getMoves().add(move);
        
        //test the generator output with the parser
        testGeneratorAndParser(expectedDrillInfo, new File(outputPath + "10-two-moves-one-rank-song-constants-renamed-command-output.pnd"));
    }
    
    private void testGeneratorAndParser(DrillInfo expectedDrillInfo, File outputFile) throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
        //generate the xml
        XMLGenerator generator = new XMLGenerator();
        generator.save(expectedDrillInfo, outputFile);
                
        //test with the parser
        XMLParser parser = new XMLParser();
        Assert.assertEquals(expectedDrillInfo, parser.load(outputFile));
    }
}
