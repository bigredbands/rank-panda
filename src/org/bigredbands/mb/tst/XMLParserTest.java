package org.bigredbands.mb.tst;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.bigredbands.mb.controllers.XMLParser;
import org.bigredbands.mb.exceptions.DrillXMLException;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.DrillInfo;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class XMLParserTest {
	
	//TODO: tests with different line types (this one is very important) and all of the commands
	
	private final String path = "tst-data/XMLParser/";
	
	@Test
	public void testEmptyProject() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "1-empty-project.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		Assert.assertEquals(new DrillInfo(), drillInfo);
	}
	
	@Test
	public void testOneMoveNoRanks() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "2-one-move-no-ranks.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		DrillInfo expectedDrillInfo = new DrillInfo();
		expectedDrillInfo.getMoves().add(new Move());
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	//TODO: NOTE: this is actually an exceptional case because
	//there were empty moves and then suddenly move three.  Fix this.
	@Test
	public void testOneMoveNoRanksNotInOrder() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "3-one-move-no-ranks-not-in-order.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		DrillInfo expectedDrillInfo = new DrillInfo();
		expectedDrillInfo.getMoves().add(new Move());
		expectedDrillInfo.getMoves().add(new Move());
		expectedDrillInfo.getMoves().add(new Move());
		Move move = new Move();
		move.setCounts(100);
		expectedDrillInfo.getMoves().add(move);
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testOneMoveNoRanksWithComments() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "4-one-move-no-ranks-with-comments.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		DrillInfo expectedDrillInfo = new DrillInfo();
		Move move = new Move();
		move.setComments("There are no ranks here!");
		expectedDrillInfo.getMoves().add(move);
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testOneTempoChange() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "5-one-tempo-change.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		DrillInfo expectedDrillInfo = new DrillInfo();
		expectedDrillInfo.getTempoHashMap().put(10, 180);
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testMultipleTempoChanges() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "6-multiple-tempo-changes.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		DrillInfo expectedDrillInfo = new DrillInfo();
		expectedDrillInfo.getTempoHashMap().put(10, 180);
		expectedDrillInfo.getTempoHashMap().put(20, 190);
		expectedDrillInfo.getTempoHashMap().put(30, 200);
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testOneCountChange() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "7-one-count-change.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		DrillInfo expectedDrillInfo = new DrillInfo();
		expectedDrillInfo.getCountsHashMap().put(10, 4);
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testMultipleCountChanges() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "8-multiple-count-changes.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//test
		DrillInfo expectedDrillInfo = new DrillInfo();
		expectedDrillInfo.getCountsHashMap().put(10, 4);
		expectedDrillInfo.getCountsHashMap().put(20, 5);
		expectedDrillInfo.getCountsHashMap().put(30, 6);
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testTwoMovesOneRankSongConstants() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "9-two-moves-one-rank-song-constants.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//create the expected values
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
		
		move.setCounts(0);
		
		move.setComments("This is the zero move");
		
		expectedDrillInfo.getMoves().add(move);
		
		//move 1
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
		
		move.setCounts(100);
		
		move.setComments("This is the first move");
		
		expectedDrillInfo.getMoves().add(move);
		
		System.out.println("Expected:\n========================\n" + expectedDrillInfo.toString() + "\n");
		System.out.println("Actual:\n========================\n" + drillInfo.toString() + "\n");
		
		//test
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testTwoMovesMultipleRanksSongConstants() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "10-two-moves-two-ranks-song-constants.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//create the expected values
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
		
		move.setCounts(0);
		
		move.setComments("This is the zero move");
		
		expectedDrillInfo.getMoves().add(move);
		
		//move 1
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
		
		move.setCounts(100);
		
		move.setComments("This is the first move");
		
		expectedDrillInfo.getMoves().add(move);
		
		System.out.println("Expected:\n========================\n" + expectedDrillInfo.toString() + "\n");
		System.out.println("Actual:\n========================\n" + drillInfo.toString() + "\n");
		
		//test
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
	
	@Test
	public void testTwoMovesOneRankSongConstantsRenamedCommand() throws ParserConfigurationException, SAXException, IOException, DrillXMLException {
		//set the file url
		File testFile = new File(path + "11-two-moves-one-rank-song-constants-renamed-command.pnd");
		
		//create the xml parser
		XMLParser parser = new XMLParser();
		
		//parse the file
		DrillInfo drillInfo = parser.load(testFile);
		
		//create the expected values
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
		
		move.setCounts(0);
		
		move.setComments("This is the zero move");
		
		expectedDrillInfo.getMoves().add(move);
		
		//move 1
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
		
		move.setCounts(100);
		
		move.setComments("This is the first move");
		
		expectedDrillInfo.getMoves().add(move);
		
		System.out.println("Expected:\n========================\n" + expectedDrillInfo.toString() + "\n");
		System.out.println("Actual:\n========================\n" + drillInfo.toString() + "\n");
		
		//test
		Assert.assertEquals(expectedDrillInfo, drillInfo);
	}
}
