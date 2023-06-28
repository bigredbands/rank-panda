package org.bigredbands.mb.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bigredbands.mb.models.MarchingConstants.PART;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Move {

    private HashMap<String, ArrayList<CommandPair>> commands;

    //NOTE: the start position variable of one rank and the end position variable of the previous rank both
    //point to the same object
    private HashMap<String, RankPosition> startPositions;
    private HashMap<String, RankPosition> endPositions;

    private String comments;
    private Integer moveCounts;

    public Move() {
        startPositions = new HashMap<String, RankPosition>();
        endPositions = new HashMap<String, RankPosition>();
        commands = new HashMap<String, ArrayList<CommandPair>>();
        comments = "";
        moveCounts = 0;
    }

    public Move(int counts, HashMap<String, RankPosition> startPositions) {
        this.startPositions = deepCopyPositions(startPositions);
        this.endPositions = deepCopyPositions(startPositions);
        commands = new HashMap<String, ArrayList<CommandPair>>();
        for (String rankName : startPositions.keySet()) {
            commands.put(rankName, new ArrayList<CommandPair>());
        }
        this.comments = "";
        this.moveCounts = counts;
    }

    public void addRank(String rankName, RankPosition rankPosition) {
        commands.put(rankName, new ArrayList<CommandPair>());
        startPositions.put(rankName, rankPosition);
        endPositions.put(rankName, new RankPosition(rankPosition));
    }

    public void deleteRank(String rankName) {
        commands.remove(rankName);
        startPositions.remove(rankName);
        endPositions.remove(rankName);
    }

    public String addCommand(String rankName, CommandPair command) {
        //check to make sure the specified rank exists
        //String rankName = (String)rankNames.toArray()[0];
        if (!commands.containsKey(rankName)) {
            return "The rank " + rankName + " does not exist.  Please use a rank that has already been created.";
        }

        //calculate to see if this command would put the move longer than it can actually be
        int totalCounts = 0;
        for (CommandPair commandPair : commands.get(rankName)) {
            totalCounts = totalCounts + commandPair.getCounts();
        }
        if (totalCounts + command.getCounts() > moveCounts) {
            return "The command you have created for " + rankName + " adds more counts than exist in the move.";
        }

        //add the new command to the commands arraylist for the rank
        commands.get(rankName).add(command);

        //calculate the new end position and store it
        endPositions.put(rankName, getPositionFromCommands(rankName));

        return "";

    }

    public String removeCommands(String rankName, int[] commandIndices) {
        //check to make sure the specified rank exists
        if (!commands.containsKey(rankName)) {
            return "The rank " + rankName + " does not exist.  Please use a rank that has already been created.";
        }

        //remove the commands from the list
        for (int i = 0; i < commandIndices.length; i++) {
            commands.get(rankName).remove(commandIndices[i]-i);  //need to subtract i because we are remove ranks as we iterate, changing their indices
        }

        //calculate the new end position and store it
        endPositions.put(rankName, getPositionFromCommands(rankName));

        return "";
    }

    public String renameCommand(String rankName, int index, String name) {
        //check to make sure the specified rank exists
        if (!commands.containsKey(rankName)) {
            return "The rank " + rankName + " does not exist.  Please use a rank that has already been created.";
        }

        //set the name of the command
        commands.get(rankName).get(index).setName(name);

        return "";
    }

    public String moveCommandsUp(String rankName, int[] commandIndices) {
        //check to make sure the specified rank exists
        if (!commands.containsKey(rankName)) {
            return "The rank " + rankName + " does not exist.  Please use a rank that has already been created.";
        }

        //create a temporary command pair to hold the command pairs while moving
        CommandPair tempCommandPair;

        //simply return for cases where you do not want to act but also do not need an error message
        //(for example, no commands selected or the commands cannot move up any more).
        if (commandIndices.length <= 0 || commandIndices[0] == 0) {
            return "";
        }

        //move the command up one position
        for (int i = 0; i < commandIndices.length; i++) {
            tempCommandPair = commands.get(rankName).remove(commandIndices[i]);
            commands.get(rankName).add(commandIndices[i]-1, tempCommandPair);
        }

        return "";
    }

    public String moveCommandsDown(String rankName, int[] commandIndices) {
        //check to make sure the specified rank exists
        if (!commands.containsKey(rankName)) {
            return "The rank " + rankName + " does not exist.  Please use a rank that has already been created.";
        }

        //create a temporary command pair to hold the command pairs while moving
        CommandPair tempCommandPair;

        //simply return for cases where you do not want to act but also do not need an error message
        //(for example, no commands selected or the commands cannot move down any more).
        if (commandIndices.length <= 0 || commandIndices[commandIndices.length-1] >= commands.get(rankName).size()-1) {
            return "";
        }

        //move the command up one position
        for (int i = commandIndices.length-1; i >= 0; i--) {
            tempCommandPair = commands.get(rankName).remove(commandIndices[i]);
            commands.get(rankName).add(commandIndices[i]+1, tempCommandPair);
        }

        return "";
    }

    public String mergeCommands(String rankName, int[] commandIndices) {
        //check to make sure the specified rank exists
        if (!commands.containsKey(rankName)) {
            return "The rank " + rankName + " does not exist.  Please use a rank that has already been created.";
        }

        //simply return for case where you do not want to act but also do not need an error message
        //(for example, no commands are selected)
        if (commandIndices.length <= 0) {
            return "";
        }

        //get the first command type
        int firstCommandType = commands.get(rankName).get(commandIndices[0]).getCommand();

        //check that all other selected commands are of the same type
        for (int i = 1; i < commandIndices.length; i++) {
            if (firstCommandType != commands.get(rankName).get(commandIndices[i]).getCommand()) {
                return "The selected commands were of different types.  You can only merge commands that have the same type.";
            }
        }

        //merge the selected commands if they are of the same type.
        //NOTE: we are throwing away any command names that have been adding before the merge
        int mergedCounts = 0;
        for (int i = 0; i < commandIndices.length; i++) {
            CommandPair tempCommand = commands.get(rankName).remove(commandIndices[i]-i);  //need to subtract i because we are remove ranks as we iterate, changing their indices
            mergedCounts = mergedCounts + tempCommand.getCounts();
        }
        commands.get(rankName).add(commandIndices[0], new CommandPair(firstCommandType, mergedCounts));

        return "";
    }

    public String splitCommand(String rankName, int index, int count) {
        //check to make sure the specified rank exists
        if (!commands.containsKey(rankName)) {
            return "The rank " + rankName + " does not exist.  Please use a rank that has already been created.";
        }

        //TODO: this check already happens in the main controller where split command is called, due to
        // where the number of counts is stored
        // and the fact that we dont want to close the dialog if this happens, merely display the warning.
        // including the check here because
        // its bad to assume that this is true, and the other check should realy be moved to here and this
        // function should return something different besides just a string
        if (count >= commands.get(rankName).get(index).getCounts()) {
            return "The specified count was larger than counts in the move.";
        }

        if (index >= commands.get(rankName).size()) {
            return "The specified index was larger than the size of the command list.";
        }

        CommandPair tempCommand = commands.get(rankName).remove(index);
        commands.get(rankName).add(index, new CommandPair(tempCommand.getCommand(), count));
        commands.get(rankName).add(index+1, new CommandPair(tempCommand.getCommand(), tempCommand.getCounts() - count));

        return "";
    }

    //TODO: assumes the rank name exists
    public void updatePositions(String rankName, RankPosition newStartPos) {
        startPositions.put(rankName, newStartPos);
        endPositions.put(rankName, getPositionFromCommands(rankName));
    }

    public HashMap<String, RankPosition> deepCopyPositions(HashMap<String, RankPosition> existingPositions) {
        HashMap<String, RankPosition> positionCopy = new HashMap<String, RankPosition>();

        for (String rankName : existingPositions.keySet()) {
            positionCopy.put(rankName, new RankPosition(existingPositions.get(rankName)));
        }

        return positionCopy;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }

    public HashMap<String, ArrayList<CommandPair>> getCommands() {
        return commands;
    }

    public void setCommands(HashMap<String, ArrayList<CommandPair>> commands) {
        this.commands= commands;
    }

    public void setStartPositions(HashMap<String, RankPosition> startPosition) {
        this.startPositions = startPosition;
    }

    public HashMap<String, RankPosition> getStartPositions() {
        return startPositions;
    }

    public void setEndPositions(HashMap<String, RankPosition> endPositions) {
        this.endPositions = endPositions;
    }

    public HashMap<String, RankPosition> getEndPositions() {
        return endPositions;
    }

    public void setCounts(int counts) {
        this.moveCounts = counts;
    }

    public int getCounts() {
        return moveCounts;
    }

    public HashMap<String, RankPosition> getIntermediatePositions(int counts) {
        HashMap<String, RankPosition> itermediatePos = new HashMap<String, RankPosition>();

        for (String rankName : startPositions.keySet()) {
            itermediatePos.put(rankName, getPositionFromCommandsAndCounts(rankName, counts));
        }

        return itermediatePos;
    }

    //TODO: not terribly efficient because it starts from the beginning of the move each time this is called,
    // and this is called from each frame of the animation
    private RankPosition getPositionFromCommandsAndCounts(String rankName, int counts) {
        RankPosition position = new RankPosition(startPositions.get(rankName));
        for (int i = 0; i < commands.get(rankName).size() && counts > 0; i++) {
            CommandPair command = commands.get(rankName).get(i);
            int countsLeft = counts - command.getCounts();

            //only do a portion of the command
            if (countsLeft < 0) {
                movePortionCounts(command.getCommand(), position, counts, command.getCounts(), command.getDestination());
            }
            //else do full command (as part of the chain that leads up to the current one
            else {
                //moveFullCounts(command, position);
                movePortionCounts(command.getCommand(), position, command.getCounts(), command.getCounts(), command.getDestination());
            }
            counts = counts - command.getCounts();

        }

        return position;
    }

    private RankPosition getPositionFromCommands(String rankName) {
        //create a new endPosition object from the current start position
        RankPosition endPosition = new RankPosition(startPositions.get(rankName));
        for (int i = 0; i < commands.get(rankName).size(); i++) {
            int numcounts = commands.get(rankName).get(i).getCounts();
            CommandPair cmd = commands.get(rankName).get(i);
            movePortionCounts(commands.get(rankName).get(i).getCommand(), endPosition, numcounts, numcounts,cmd.getDestination());
        }
        return endPosition;
    }

    //TODO: can definitely combine these two.  is it worth it?
    private void movePortionCounts(int command, RankPosition endPosition, int counts,
            int totalcounts, RankPosition dest) {

        switch (command){
            case CommandPair.MT:
                break;
            case CommandPair.HALT:
                break;
            case CommandPair.FM:
                endPosition.incrementPointsYValue(MarchingConstants.STANDARD_STEP_SIZE * counts);
                break;
            case CommandPair.BM:
                endPosition.incrementPointsYValue(MarchingConstants.STANDARD_STEP_SIZE * counts * -1);
                break;
            case CommandPair.RS:
                endPosition.incrementPointsXValue(MarchingConstants.STANDARD_STEP_SIZE * counts * -1);
                break;
            case CommandPair.LS:
                endPosition.incrementPointsXValue(MarchingConstants.STANDARD_STEP_SIZE * counts);
                break;
            case CommandPair.CURVE_LEFT:
                endPosition.curveMoveAuto(MarchingConstants.STANDARD_STEP_SIZE * counts, 0);
                break;
            case CommandPair.CURVE_RIGHT:
                endPosition.curveMoveAuto(MarchingConstants.STANDARD_STEP_SIZE * counts, 1);
                break;
            case CommandPair.FLAT_TO_ENDS:
                endPosition.flattenMidMove((float)counts/(float)totalcounts);
                break;
            case CommandPair.FLAT_TO_MID:
                endPosition.flattenEndsMove((float)counts/(float)totalcounts);
                break;
            case CommandPair.GTCW_HEAD:
                endPosition.gateTurnMove(-1*MarchingConstants.STANDARD_GATE_TURN_RATIO*counts, PART.HEAD);
                break;
            case CommandPair.GTCW_TAIL:
                endPosition.gateTurnMove(-1*MarchingConstants.STANDARD_GATE_TURN_RATIO*counts, PART.TAIL);
                break;
            case CommandPair.GTCCW_HEAD:
                endPosition.gateTurnMove(MarchingConstants.STANDARD_GATE_TURN_RATIO*counts, PART.HEAD);
                break;
            case CommandPair.GTCCW_TAIL:
                endPosition.gateTurnMove(MarchingConstants.STANDARD_GATE_TURN_RATIO*counts, PART.TAIL);
                break;
            case CommandPair.PWCW:
                endPosition.pinwheelMove(MarchingConstants.STANDARD_PINWHEEL_RATIO*counts);
                break;
            case CommandPair.PWCCW:
                endPosition.pinwheelMove(-1*MarchingConstants.STANDARD_PINWHEEL_RATIO*counts);
                break;
            case CommandPair.EXPAND_HEAD:
                endPosition.expansionMove(MarchingConstants.STANDARD_STEP_SIZE * counts, 0.0f);
                break;
            case CommandPair.EXPAND_TAIL:
                endPosition.expansionMove(0.0f, MarchingConstants.STANDARD_STEP_SIZE * counts);
                break;
            case CommandPair.EXPAND_BOTH:
                endPosition.expansionMove(MarchingConstants.STANDARD_STEP_SIZE * counts/2, MarchingConstants.STANDARD_STEP_SIZE * counts/2);
                break;
            case CommandPair.CONDENSE_HEAD:
                endPosition.expansionMove(MarchingConstants.STANDARD_STEP_SIZE * counts *-1, 0.0f);
                break;
            case CommandPair.CONDENSE_TAIL:
                endPosition.expansionMove(0.0f, MarchingConstants.STANDARD_STEP_SIZE * counts *-1);
                break;
            case CommandPair.CONDENSE_BOTH:
                endPosition.expansionMove(MarchingConstants.STANDARD_STEP_SIZE * counts*-1/2, MarchingConstants.STANDARD_STEP_SIZE * counts*-1/2);
                break;
            case CommandPair.DTP:
                // TODO: implement
                endPosition.directMove(dest, (float)counts/(float)totalcounts);
                break;

            case CommandPair.FTA:
                // TODO: implement
                break;

            case CommandPair.CORNER_LB:
                endPosition.cornerMove((float)counts/(float)totalcounts, 1, -1, 0);
                break;

            case CommandPair.CORNER_LF:
                endPosition.cornerMove((float)counts/(float)totalcounts, 1, 1, 0);
                break;

            case CommandPair.CORNER_RB:
                endPosition.cornerMove((float)counts/(float)totalcounts, -1, -1, 0);
                break;

            case CommandPair.CORNER_RF:
                endPosition.cornerMove((float)counts/(float)totalcounts, -1, 1, 0);
                break;

            case CommandPair.CORNER_FR:
                endPosition.cornerMove((float)counts/(float)totalcounts, -1, 1, 1);
                break;

            case CommandPair.CORNER_FL:
                endPosition.cornerMove((float)counts/(float)totalcounts, 1, 1, 1);
                break;

            case CommandPair.CORNER_BR:
                endPosition.cornerMove((float)counts/(float)totalcounts, -1, -1, 1);
                break;

            case CommandPair.CORNER_BL:
                endPosition.cornerMove((float)counts/(float)totalcounts, 1, -1, 1);
                break;

        }
    }

    /**
     * DEPRECATED - DO not use
     *
     * @param command
     * @param endPosition

    private void moveFullCounts(CommandPair command, RankPosition endPosition) {
        //do not add mt, as that does not change the position
        //do not add halt
        if (command.getCommand() == CommandPair.FM) {
            endPosition.incrementPointsYValue(MarchingConstants.STANDARD_STEP_SIZE * command.getCounts());
        }
        else if (command.getCommand() == CommandPair.BM) {
            endPosition.incrementPointsYValue(MarchingConstants.STANDARD_STEP_SIZE * command.getCounts() * -1);
        }
        else if (command.getCommand() == CommandPair.RS) {
            endPosition.incrementPointsXValue(MarchingConstants.STANDARD_STEP_SIZE * command.getCounts() * -1);
        }
        else if (command.getCommand() == CommandPair.LS) {
            endPosition.incrementPointsXValue(MarchingConstants.STANDARD_STEP_SIZE * command.getCounts());
        }
    }*/

    @Override
    public boolean equals(Object obj) {
        //check basic equality
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        //check field equality
        Move other = (Move) obj;
        if (commands == null) {
            if (other.commands != null) {
                return false;
            }
        }
        else if (!commands.equals(other.commands)) {
            return false;
        }

        if (endPositions == null) {
            if (other.endPositions != null) {
                return false;
            }
        }
        else if (!endPositions.equals(other.endPositions)) {
            return false;
        }

        if (startPositions == null) {
            if (other.startPositions != null) {
                return false;
            }
        }
        else if (!startPositions.equals(other.startPositions)) {
            return false;
        }

        if (moveCounts == null) {
            if (other.moveCounts != null) {
                return false;
            }
        }
        else if (!moveCounts.equals(other.moveCounts)) {
            return false;
        }

        //if passes all tests, return true
        return true;
    }

    @Override
    public String toString() {
        return "Move [commands=" + commands + ", startPositions="
                + startPositions + ", endPositions=" + endPositions
                + ", comments=" + comments + ", moveCounts=" + moveCounts + "]";
    }

    public Element convertToXML(Document document, Integer moveNumber) {
        //create the move tag
        Element moveTag = document.createElement(XMLConstants.MOVE);

        //add the move number tag
        Element moveNumberTag = document.createElement(XMLConstants.MOVE_NUMBER);
        moveTag.appendChild(moveNumberTag);

        //add the move number text to the tag
        Text moveNumberText = document.createTextNode(moveNumber.toString());
        moveNumberTag.appendChild(moveNumberText);

        //add the move counts tag for the move
        Element moveCountsTag = document.createElement(XMLConstants.MOVE_COUNTS);
        moveTag.appendChild(moveCountsTag);

        //add the move counts text to the tag
        Text moveCountsText = document.createTextNode(moveCounts.toString());
        moveCountsTag.appendChild(moveCountsText);

        //add the rank tags with info
        //TODO: NOTE: assuming that each hashmap has the same ranks
        //get the start position iterator
        Set<String> startKeys = startPositions.keySet();
        Iterator<String> startIterator = startKeys.iterator();

        //get the commands iterator
        Set<String> commandKeys = commands.keySet();
        Iterator<String> commandIterator = commandKeys.iterator();

        while (startIterator.hasNext()) {
            //get the rank name
            String rankName = startIterator.next();

            //add the rank parent tag
            Element rankTag = document.createElement(XMLConstants.RANK);
            moveTag.appendChild(rankTag);

            //add the rank name tag
            Element rankNameTag = document.createElement(XMLConstants.RANK_NAME);
            rankTag.appendChild(rankNameTag);

            //add the rank name text to the tag
            Text rankNameText = document.createTextNode(rankName);
            rankNameTag.appendChild(rankNameText);

            //add the start position tag
            Element startPositionTag = startPositions.get(rankName).convertToXML(document, XMLConstants.START_POS);
            rankTag.appendChild(startPositionTag);

            //add the command tags
            ArrayList<CommandPair> commandList = commands.get(rankName);
            for (int i = 0; i < commandList.size(); i++) {
                Element commandTag = commandList.get(i).convertToXML(document, i);
                rankTag.appendChild(commandTag);
            }

            //add the end position tag
            Element endPositionTag = endPositions.get(rankName).convertToXML(document, XMLConstants.END_POS);
            rankTag.appendChild(endPositionTag);
        }

        //add the comments tag
        Element commentsTag = document.createElement(XMLConstants.MOVE_COMMENTS);
        moveTag.appendChild(commentsTag);

        //add the comments text to the tag
        Text commentsText = document.createTextNode(comments);
        commentsTag.appendChild(commentsText);
        return moveTag;
    }
}
