package org.bigredbands.mb.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bigredbands.mb.exceptions.DrillXMLException;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.DrillInfo;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;
import org.bigredbands.mb.models.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

    //TODO: need some form of a verifier eventually after reading from the xml file
    //to make sure all the data is sound

    public XMLParser() {

    }

    /**
     * Loads the models based on the information stored in a save XML file.
     *
     * @param file - the save file stored in XML format
     * @return a list of moves read from the save XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws DrillXMLException
     */
    public DrillInfo load(File file) throws ParserConfigurationException, SAXException, IOException, DrillXMLException{
        //creates an empty list of moves and defines the a SongConstants variable
        DrillInfo drillInfo = new DrillInfo();

        //create document builder and get the document
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(file);

        //creates NodeList of all move tags in the document
        NodeList moveList = doc.getElementsByTagName(XMLConstants.MOVE);

        //iterating through list of moves to find its children
        for (int i = 0; i < moveList.getLength(); i++){
            //get the individual move from the list
            Node move = moveList.item(i);

            //add the move to the list
            addMoveToList(move.getChildNodes(), drillInfo.getMoves());
        }

        //create a NodeList of all the tempo-change tags in the document
        NodeList tempoChangeList = doc.getElementsByTagName(XMLConstants.TEMPO_CHANGE);

        //set the tempo change hashmap
        for (int i = 0; i < tempoChangeList.getLength(); i++) {
            //get the individual tempo change from the list
            Node tempoChange = tempoChangeList.item(i);

            addSongConstantToHashMap(drillInfo.getTempoHashMap(), tempoChange.getChildNodes(), XMLConstants.TEMPO);
        }

        //create a NodeList of all the counts-per-measure-change tags in the document
        NodeList countsPerMeasureChangeList = doc.getElementsByTagName(XMLConstants.COUNT_PER_MEASURE_CHANGE);

        //set the counts per measure change hashmap
        for (int i = 0; i < countsPerMeasureChangeList.getLength(); i++) {
            //get the individual counts per measure change from the list
            Node countsPerMeasuresChange = countsPerMeasureChangeList.item(i);

            addSongConstantToHashMap(drillInfo.getCountsHashMap(), countsPerMeasuresChange.getChildNodes(), XMLConstants.COUNT_PER_MEASURE);
        }

        //create a NodeList of the song-name tags in the document
        NodeList songNameList = doc.getElementsByTagName(XMLConstants.SONG_NAME);

        //set the song name appropriately
        if (songNameList.getLength() > 0) {
            Node songName = songNameList.item(0);
            drillInfo.setSongName(getTagText(songName));
        }
        
        return drillInfo;
    }

    /**
     * Creates the move object and adds it to the list of moves at the specified location.
     *
     * @param moveChildren - the children of the given move tag
     * @param moves - the list of moves that the new Move object will be added to
     * @throws DrillXMLException
     */
    private void addMoveToList(NodeList moveChildren, List<Move> moves) throws DrillXMLException {
        //create the new move
        Move move = new Move();
        int moveNumber = -1;
        int moveCounts = -1;

        //iterate through the children of the move tag
        for (int i=0; i < moveChildren.getLength(); i++) {
            Node moveChild = moveChildren.item(i);
            //ignore non tag children of the move tag
            if (moveChild.getNodeType() == Node.ELEMENT_NODE) {
                //get the rank information if we find a rank
                if (moveChild.getNodeName().equals(XMLConstants.RANK)) {
                    addRankToMove(moveChild.getChildNodes(), move);
                }
                //get the move number if we find the number tag
                else if (moveChild.getNodeName().equals(XMLConstants.MOVE_NUMBER)) {
                    if (moveNumber == -1) {
                        moveNumber = Integer.parseInt(getTagText(moveChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were two " + XMLConstants.MOVE_NUMBER + " tags in a move.");
                    }
                }
                //get the number of counts in the move if we find the move counts tag
                else if (moveChild.getNodeName().equals(XMLConstants.MOVE_COUNTS)) {
                    if (moveCounts == -1) {
                        moveCounts = Integer.parseInt(getTagText(moveChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were two " + XMLConstants.MOVE_COUNTS + " tags in a move.");
                    }
                }
                //get the comments information if we find a comment
                else if (moveChild.getNodeName().equals(XMLConstants.MOVE_COMMENTS)) {
                    move.setComments(getTagText(moveChild));
                }
            }
        }

        //check if a move number and counts have been specified, as this is required
        if (moveNumber == -1) {
            throw new DrillXMLException("DrillXMLException: a move number was not specified for a move.");
        }
        if (moveCounts == -1) {
            throw new DrillXMLException("DrillXMLException: the number of counts for a move was not specified for a move.");
        }

        //store the move number in the move
        move.setCounts(moveCounts);

        //TODO: check if the move already exists and is not empty

        //add the move to the list at its specified move number, adding other empty moves
        //before it that may not have been read yet
        while (moveNumber > moves.size()) {
            moves.add(new Move());
        }
        moves.add(move);
    }

    /**
     * Adds the ranks of the move to the given move.
     *
     * @param rankChildren - the children of the given rank
     * @param move - the move the rank will be added to
     * @throws DrillXMLException
     */
    private void addRankToMove(NodeList rankChildren, Move move) throws DrillXMLException {
        //initialize the rank name
        String rankName = null;
        ArrayList<CommandPair> commands = new ArrayList<CommandPair>();
        RankPosition startPosition = null;
        RankPosition endPosition = null;

        //iterate through the children of the rank tag
        for (int i = 0; i < rankChildren.getLength(); i++) {
            Node rankChild = rankChildren.item(i);
            //ignore non tag children of the rank tag
            if (rankChild.getNodeType() == Node.ELEMENT_NODE) {
                if (rankChild.getNodeName().equals(XMLConstants.RANK_NAME)) {
                    if (rankName == null) {
                        rankName = getTagText(rankChild);
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple rank names for one rank.");
                    }
                }
                else if (rankChild.getNodeName().equals(XMLConstants.START_POS)) {
                    if (startPosition == null) {
                        startPosition = getRankPosition(rankChild.getChildNodes());
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple start positions for one rank.");
                    }
                }
                else if (rankChild.getNodeName().equals(XMLConstants.COMMAND)) {
                    addCommandsToList(commands, rankChild.getChildNodes());
                }
                else if (rankChild.getNodeName().equals(XMLConstants.END_POS)) {
                    if (endPosition == null) {
                        endPosition = getRankPosition(rankChild.getChildNodes());
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple end positions for one rank.");
                    }
                }
            }
        }

        if (rankName == null) {
            throw new DrillXMLException("DrillXMLException: No rank name was specified for a rank.");
        }
        if (startPosition == null) {
            throw new DrillXMLException("DrillXMLException: No start position was specified for a rank.");
        }
        if (endPosition == null) {
            throw new DrillXMLException("DrillXMLException: No end position was specified for a rank.");
        }

        move.getCommands().put(rankName, commands);
        move.getStartPositions().put(rankName, startPosition);
        move.getEndPositions().put(rankName, endPosition);
    }

    /**
     * Adds commands to list of commands in move for rank
     * @param commands - ArrayList of commands for rank
     * @param commandChildren - NodeList of commands
     * @throws DrillXMLException
     */
    private void addCommandsToList(ArrayList<CommandPair> commands, NodeList commandChildren) throws DrillXMLException{
        //create the new move
        int commandIndex = -1;
        int counts = -1;
        int commandType = -1;
        String commandName = "";
        RankPosition DTPDest = null;

        //iterate through the children of the tag
        for (int i=0; i < commandChildren.getLength(); i++) {
            Node commandChild = commandChildren.item(i);
            //ignore non tag children of the tag
            if (commandChild.getNodeType() == Node.ELEMENT_NODE) {
                //get the rank information if we find a rank
                if (commandChild.getNodeName().equals(XMLConstants.INDEX)) {
                    if (commandIndex == -1) {
                        commandIndex = Integer.parseInt(getTagText(commandChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were two index tags in a command.");
                    }
                }
                //get the move number if we find the number tag
                else if (commandChild.getNodeName().equals(XMLConstants.COMMAND_TYPE)) {
                    if (commandType == -1) {
                        //TODO: assuming it will be a recognized command
                        commandType = Integer.parseInt(getTagText(commandChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were two command-types tags in a command.");
                    }
                }
                //get the counts information if we find the counts tag
                else if (commandChild.getNodeName().equals(XMLConstants.COUNTS)) {
                    if (counts == -1) {
                        counts = Integer.parseInt(getTagText(commandChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were two counts tags in a command.");
                    }
                }
                //get the command name information if we find the command name tag
                else if (commandChild.getNodeName().equals(XMLConstants.COMMAND_NAME)) {
                    if (commandName.isEmpty()) {
                        commandName = getTagText(commandChild);
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were two command name tags in a command.");
                    }
                }
                else if (commandType==CommandPair.DTP &&
                        commandChild.getNodeName().equals(XMLConstants.DESTINATION)) {
                    if (DTPDest==null) {
                        String[] tokens = getTagText(commandChild).split(";");
                        if(tokens.length!=4) {
                            throw new DrillXMLException("DrillXMLException: DTP destination not encoded properly.");
                        }
                        int lineType = Integer.parseInt(tokens[0]);
                        String[] frontStr = tokens[1].split(",");
                        if(frontStr.length!=2) {
                            throw new DrillXMLException("DrillXMLException: DTP destination not encoded properly.");
                        }
                        Point front = new Point(Float.parseFloat(frontStr[0]),Float.parseFloat(frontStr[1]));

                        String[] midStr = tokens[2].split(",");
                        if(midStr.length!=2) {
                            throw new DrillXMLException("DrillXMLException: DTP destination not encoded properly.");
                        }
                        Point mid = new Point(Float.parseFloat(midStr[0]),Float.parseFloat(midStr[1]));

                        String[] endStr = tokens[3].split(",");
                        if(endStr.length!=2) {
                            throw new DrillXMLException("DrillXMLException: DTP destination not encoded properly.");
                        }
                        Point end = new Point(Float.parseFloat(endStr[0]),Float.parseFloat(endStr[1]));

                        DTPDest = new RankPosition(front,mid,end,lineType);
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were two destination tags in a command.");
                    }
                }
            }
        }

        //check if all of the necessary fields have been specified
        if (commandIndex == -1) {
            throw new DrillXMLException("DrillXMLException: a command index was not specified for a command.");
        }
        if (commandType == -1) {
            throw new DrillXMLException("DrillXMLException: a command type was not specified for a command.");
        }
        if (counts == -1) {
            throw new DrillXMLException("DrillXMLException: the counts were not specified for a command.");
        }

        //check if the command already exists and is not empty
        if (commands.size() > commandIndex && !commands.get(commandIndex).isEmpty()) {
            throw new DrillXMLException("DrillXMLException: the command already existed");
        }

        //add the move to the list at its specified move number, adding other empty moves
        //before it that may not have been read yet
        while (commandIndex > commands.size()) {
            commands.add(new CommandPair());
        }
        CommandPair cmd = new CommandPair(commandType, counts, commandName);
        if(commandType == CommandPair.DTP) {
            cmd.setDestination(DTPDest);
        }
        commands.add(cmd);
    }

    /**
     * Gets rank position
     *
     * @param positionChildren
     * @return
     * @throws DrillXMLException
     */
    private RankPosition getRankPosition(NodeList positionChildren) throws DrillXMLException {
        //initialize points
        Point startPoint = null;
        Point pointOne = null;
        Point endPoint = null;
        int lineType = -1;

        //iterate through the children of the position tag
        for (int i = 0; i < positionChildren.getLength(); i++) {
            Node positionChild = positionChildren.item(i);
            //ignore non tag children of the rank tag
            if (positionChild.getNodeType() == Node.ELEMENT_NODE) {
                if (positionChild.getNodeName().equals(XMLConstants.LINE_TYPE)) {
                    if (lineType == -1) {
                        lineType = Integer.parseInt(getTagText(positionChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple line types for one rank.");
                    }
                }
                else if (positionChild.getNodeName().equals(XMLConstants.FRONT_POINT)) {
                    if (startPoint == null) {
                        startPoint = getPoint(positionChild.getChildNodes());
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple start points for one rank.");
                    }
                }
                else if (positionChild.getNodeName().equals(XMLConstants.POINT_ONE)) {
                    if (pointOne == null) {
                        pointOne = getPoint(positionChild.getChildNodes());
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple point ones for one rank.");
                    }
                }
                else if (positionChild.getNodeName().equals(XMLConstants.END_POINT)) {
                    if (endPoint == null) {
                        endPoint = getPoint(positionChild.getChildNodes());
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple end points for one rank.");
                    }
                }
            }
        }

        //sanity checks after parsing
        if (startPoint == null) {
            throw new DrillXMLException("DrillXMLException: there was no start point for a rank.");
        }
        if (pointOne == null) {
            throw new DrillXMLException("DrillXMLException: there was no point one for a rank.");
        }
        if (endPoint == null) {
            throw new DrillXMLException("DrillXMLException: there was no end point for a rank.");
        }
        if (lineType == -1) {
            throw new DrillXMLException("DrillXMLException: there was no line type for a rank.");
        }

        if (lineType == RankPosition.LINE) {
            return new RankPosition(startPoint, endPoint);
        }
        if (lineType == RankPosition.CURVE) {
            return new RankPosition(startPoint, pointOne, endPoint, RankPosition.CURVE);
        }
        if (lineType == RankPosition.CORNER) {
            return new RankPosition(startPoint, pointOne, endPoint, RankPosition.CORNER);
        }

        throw new DrillXMLException("DrillXMLException: there were multiple line types for one rank.");
    }

    private Point getPoint(NodeList pointChildren) throws DrillXMLException {
        //intialize the variables
        float x = -1;
        float y = -1;

        //iterate through the children of the song-constants tag
        for (int i = 0; i < pointChildren.getLength(); i++) {
            Node pointChild = pointChildren.item(i);
            //ignore non tag children of the rank tag
            if (pointChild.getNodeType() == Node.ELEMENT_NODE) {
                if (pointChild.getNodeName().equals(XMLConstants.X_COORDINATE)) {
                    if (x < 0) {
                        x = Float.parseFloat(getTagText(pointChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple x coordinates in a point.");
                    }
                }
                else if (pointChild.getNodeName().equals(XMLConstants.Y_COORDINATE)) {
                    if (y < 0) {
                        y = Float.parseFloat(getTagText(pointChild));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple y coordinates in a point.");
                    }
                }
            }
        }

        //check to make sure we found a key and value
        if (x < 0) {
            throw new DrillXMLException("DrillXMLException: found a point change without an x value.");
        }
        if (y < 0) {
            throw new DrillXMLException("DrillXMLException: found a point change without an y value.");
        }

        return new Point(x, y);
    }
    private void addSongConstantToHashMap(HashMap<Integer, Integer> hashMap, NodeList children, String valueTagName) throws NumberFormatException, DrillXMLException {
        //intialize the key and the value for the hashmap
        int measure = -1;
        int value = -1;

        //iterate through the children of the song-constants tag
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            //ignore non tag children of the rank tag
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals(XMLConstants.MEASURE_NUMBER)) {
                    if (measure == -1) {
                        measure = Integer.parseInt(getTagText(child));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple measure-number tags " + valueTagName + " change tag.");
                    }
                }
                else if (child.getNodeName().equals(valueTagName)) {
                    if (value == -1) {
                        value = Integer.parseInt(getTagText(child));
                    }
                    else {
                        throw new DrillXMLException("DrillXMLException: there were multiple " + valueTagName + " tags " + valueTagName + " change tag.");
                    }
                }
            }
        }

        //check to make sure we found a key and value
        if (measure == -1) {
            throw new DrillXMLException("DrillXMLException: found a " + valueTagName + " change without a measure-number tag.");
        }
        if (value == -1) {
            throw new DrillXMLException("DrillXMLException: found a " + valueTagName + " change without a " + valueTagName + " tag.");
        }

        //add the key and value to the hashmap
        hashMap.put(measure, value);
    }

    /**
     * Returns the text from a tag containing text.
     * For example, returns Tim from <name>Tim</name>
     *
     * @return the text of a node containing text.
     * @throws DrillXMLException
     */
    private String getTagText(Node parentNode) throws DrillXMLException {
        //check to make sure there is only one child node under t
        if (parentNode.getChildNodes().getLength() > 1) {
            throw new DrillXMLException("DrillXMLException: expected there to be one TEXT_NODE child under "
                    + parentNode.getNodeName() + " but there were " + parentNode.getChildNodes().getLength()
                    + " nodes.");
        }

        //get the text
        Node node = parentNode.getFirstChild();
        if (node != null) {
            if (node.getNodeType() == Node.TEXT_NODE) {
                return node.getNodeValue();
            }
            //throw an exception if the child node is a non text node
            else {
                throw new DrillXMLException("DrillXMLException: expected there to be one TEXT_NODE child under "
                        + parentNode.getNodeName() + " but there was a " + node.getNodeType() + " type");
            }
        }
        return "";
    }
}