package org.bigredbands.mb.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.bigredbands.mb.exceptions.DrillXMLException;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.DrillInfo;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.models.RankPosition;
import org.bigredbands.mb.views.MainView;
import org.bigredbands.mb.views.ViewInterface;
import org.xml.sax.SAXException;

/**
 *
 * This is the main controller that handles any updates to the models
 * and any inputs from the user to the view
 *
 */
public class MainController implements ControllerInterface, SynchronizedController {
    //TODO: consider making this class a singleton?

    // The mainView to which information will be presented to the user
    private ViewInterface mainView;

    // The save location of the project
    private String fileUrl;

    // The main model which stores the ranks and moves for the project
    private DrillInfo drillInfo;

    // The number of the current move
    private int currentMove;

    // The name of the selected rank
    private HashSet<String> selectedRanks = new HashSet<String>();

    // The playback thread used exclusively when displaying playback to the user
    private Thread playbackThread = null;

    // True if playback is currently running, false if not
    private boolean playbackRunning = false;

    // The current count of the playback animation
    private int playbackCount = 0;

    // The current move of the playback animation
    private int playbackMove = 1;

    private int playbackCountTotal = 0;

    private boolean modified = false;

    /**
     * A simple wrapper class containing a MainController and a MainView.
     */
    public static class ControllerViewBundle {
        private MainController mainController;
        private MainView mainView;

        public ControllerViewBundle(MainController mainController, MainView mainView) {
            this.mainController = mainController;
            this.mainView = mainView;
        }

        public MainController getController() {
            return mainController;
        }

        public MainView getView() {
            return mainView;
        }
    }

    /**
     * Creates a fully initialized MainController and MainView.
     * 
     * Note: The MainController and MainView have a circular dependency where each
     * needs a reference to the other. This method serves as a patch to make sure
     * each are fully initialized. Longterm, this circular dependency should be
     * resolved.
     * 
     * @return a ControllerViewBundle containing the fully initialized
     *         MainController and MainView.
     */
    public static ControllerViewBundle BuildMainControllerAndView() {
        final MainController mainController = new MainController();
        final MainView mainView = new MainView(mainController);
        mainController.initializeWithMainView(mainView);
        return new ControllerViewBundle(mainController, mainView);
    }

    /**
     * Creates a MainController class. Not fully intitialized until
     * initializeWithMainView is called.
     * 
     * Generally, BuildMainControllerAndView should be preferred, as this ensures
     * both the MainController and MainView are intialized correctly.
     */
    public MainController() {
        fileUrl = "";
        drillInfo = new DrillInfo();
        currentMove = 0;
        selectedRanks.clear();
    }

    /**
     * Initializes the MainController with a MainView.
     */
    public void initializeWithMainView(ViewInterface mainView) {
        this.mainView = mainView;
    }

    /**
     * Saves the project at its current location
     */
    @Override
    public void saveProject() {
        saveProject(new File(fileUrl));
    }

    /**
     * Saves the project at the location fileUrl
     *
     * @param file - the location to save the file at
     */
    @Override
    public void saveProject(File file) {
        //if the given file was null, do nothing
        //TODO: possibly return a warning that something went wrong
        if (file == null) {
            return;
        }

        XMLGenerator generator = new XMLGenerator();
        generator.save(drillInfo, file);

        //TODO: assuming nothing went wrong with the save...
        fileUrl = file.getAbsolutePath();
        modified = false;
    }

    /**
     * Loads the project at the location fileUrl
     *
     * @param file - the file to be loaded
     * @return true if the file is successfully loaded, false if an error occurs
     */
    @Override
    public boolean loadProject(File file) {
        //if the given file was null, do nothing
        //TODO: do other basic file checks
        if (file == null) {
            return false;
        }

        XMLParser parser = new XMLParser();

        try {
            drillInfo = parser.load(file);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            mainView.displayError(e.getMessage());
            return false;
        } catch (SAXException e) {
            e.printStackTrace();
            mainView.displayError(e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            mainView.displayError(e.getMessage());
            return false;
        } catch (DrillXMLException e) {
            e.printStackTrace();
            mainView.displayError(e.getMessage());
            return false;
        }

        currentMove = 0;
        selectedRanks.clear();

        if (!mainView.isProjectViewCreated()) {
            mainView.createProjectView(false);
        }
        mainView.updateViewWithMoves(drillInfo.getMoves().size(), currentMove, drillInfo.getMoves().get(currentMove).getCounts());

        System.out.println(drillInfo.toString());

        fileUrl = file.getAbsolutePath();
        return true;
    }

    /**
     * Creates a new blank project
     */
    @Override
    public void createEmptyProject() {
        //TODO: finish me
        fileUrl = "";
        drillInfo = new DrillInfo();
        drillInfo.getMoves().add(new Move());
        currentMove = 0;
        selectedRanks.clear();

        if (!mainView.isProjectViewCreated()) {
            mainView.createProjectView(true);
        }
        mainView.updateViewWithMoves(drillInfo.getMoves().size(), currentMove, drillInfo.getMoves().get(currentMove).getCounts());
    }

    /**
     * Checks if this project has been previously saved
     *
     * @return true if this project has not been saved before, false if it has
     */
    @Override
    public boolean isInitialSave() {
        return fileUrl.equals("");
    }

    /**
     * Exits the program cleanly
     */
    @Override
    public void closeProgram() {
        // TODO do proper clean up, dont just exit and kill everything
        System.exit(0);
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    /**
     * Returns each of the rank's name and corresponding position at the current
     * move
     *
     * @return - a hashmap mapping each rank name to its position
     */
    @Override
    public HashMap<String, RankPosition> getRankPositions() {
        return getRankPositions(currentMove);
    }

    /**
     * Returns each of the rank's name and corresponding position at the
     * specified move number
     *
     * @param moveNumber - the move number of which to get the rank names and positions
     * @return - a hashmap mapping each rank name to its position
     */
    @Override
    public HashMap<String, RankPosition> getRankPositions(int moveNumber) {
        if(moveNumber >= 0 && moveNumber < getNumberOfMoves()) {
            return drillInfo.getMoves().get(moveNumber).getEndPositions();
        } else {
            return new HashMap<String,RankPosition>();
        }
    }

    /**
     * Adds a new move to the list of moves
     *
     * TODO: may change the return type later
     * @param counts - the number of counts in the move
     */
    @Override
    public void addMove(int counts) {
        // TODO: sanity checks
        // (ex. if counts is negative or 0, incremental number of measures (if that's an invariant?), etc.)
        if(counts <= 0) {
            return;
        }
        drillInfo.addMove(counts, currentMove + 1);
        currentMove = currentMove + 1;
        if (!selectedRanks.isEmpty()) {
            mainView.updateSelectedRank(selectedRanks);
        }
        modified = true;
        mainView.updateViewWithOneMove(drillInfo.getMoves().size() - 1, drillInfo.getMoves().get(drillInfo.getMoves().size() - 1).getCounts());
    }

    /**
     * Removes one move from the list of moves
     *
     * TODO: may change the return type later
     * @param moveNum - index of the move to delete
     */
    @Override
    public void deleteMove(int moveNum) {
        // TODO: sanity checks
        if(moveNum <= 0 || moveNum >= drillInfo.getMoves().size()) {
            return;
        }
        drillInfo.deleteMove(moveNum);
        currentMove = moveNum-1;

        for(String rankName : mainView.getRankPositions().keySet()) {
            updatePositions(rankName);
        }

        if (!selectedRanks.isEmpty()) {
            mainView.updateSelectedRank(selectedRanks);
        }
        modified = true;
        mainView.updateViewWithRemoveMove(currentMove, drillInfo.getMoves().get(currentMove).getCounts(),moveNum);
    }

    /**
     * Returns the number of moves in the project
     *
     * @return - the number of moves
     */
    @Override
    public int getNumberOfMoves() {
        return drillInfo.getMoves().size();
    }

    /**
     * Switches the displayed move to the specified move
     *
     * @param targetMove - the new move which to display in the view
     */
    @Override
    public void changeMoves(int targetMove) {
        if (targetMove != currentMove) {
            currentMove = targetMove;
            if (!selectedRanks.isEmpty()) {
                mainView.updateSelectedRank(selectedRanks);
            }
            mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
        }
    }

    /**
     * Adds a new rank to the models and update the view.
     *
     * @param name - The name of the new rank
     * @param rankPosition - The position of the new rank
     * @return - An error messsge iff adding a rank failed. An empty string
     *         otherwise.
     */
    @Override
    public String addRank(String rankName, RankPosition rankPosition) {
        final String errorMessage = drillInfo.addRankToMoves(rankName, rankPosition);
        if (!errorMessage.isEmpty()) {
            return errorMessage;
            
        }

        selectedRanks.clear();
        selectedRanks.add(rankName);
        modified = true;
        mainView.updateSelectedRank(selectedRanks);
        mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
        return "";

    }

    public void updateInitialPosition(String rankName, RankPosition newPos) {
        drillInfo.getMoves().get(0).updatePositions(rankName,newPos);
        updatePositions(rankName);

    }

    public ArrayList<CommandPair> getSharedCommands(HashSet<String> rankNames, HashMap<String,ArrayList<CommandPair>>commands) {
        if(rankNames.size()==0) {
            return new ArrayList<CommandPair>();
        }
        else if(rankNames.size()==1) {
            return commands.get(rankNames.toArray()[0]);
        }
        else {
            ArrayList<CommandPair> shared = new ArrayList<CommandPair>();

            int currentCount = 0;
            int diffCounts = 0;
            String[] rankArray = rankNames.toArray(new String[0]);

            ArrayList<CommandPair> referenceCmds = commands.get(rankArray[0]);
            for (CommandPair refCmd : referenceCmds) {
                // Loop through each of the reference commands
                for (int i = 1; i < rankArray.length; i++) {
                    ArrayList<CommandPair> cmdList = commands.get(rankArray[i]);
                    int altCounts = 0;
                    boolean checkFinished = false;

                    // for THIS rank, loop through its commands until you find matching/not matching
                    for (CommandPair altCmd : cmdList) {
                        // Note: currentCount, at any time, is the FIRST count in the command (zero indexed)
                        if (altCounts == currentCount) {
                            if (altCmd.getCounts() == refCmd.getCounts()
                                    && altCmd.getCommand() == refCmd.getCommand()) {
                                if (i == rankArray.length - 1) {
                                    if (diffCounts != 0) shared.add(new CommandPair(CommandPair.EMPTY, diffCounts));
                                    diffCounts = 0;

                                    shared.add(new CommandPair(refCmd.getCommand(), refCmd.getCounts()));
                                }

                                break;
                            } else {
                                // Commands don't match - similar to overshoot, just add refCmd's counts to diff
                                diffCounts += refCmd.getCounts();
                                checkFinished = true;
                                break;
                            }
                        } else if (altCounts > currentCount) {
                            // overshoot with altCounts - means every command mismatches, so we mark as such
                            diffCounts += refCmd.getCounts();
                            checkFinished = true;
                            break;
                        }

                        altCounts += altCmd.getCounts();
                    }

                    // if we exit the loop and refCmd is too high to reach, add to diffCounts
                    if (altCounts < currentCount) {
                        diffCounts += refCmd.getCounts();
                        checkFinished = true;
                    }

                    if (checkFinished) break;
                }
                currentCount += refCmd.getCounts();
            }

            int maxLen = -1;
            for (String rankName : rankNames) {
                ArrayList<CommandPair> cmdList = commands.get(rankName);

                int len = 0;
                for (CommandPair cmd : cmdList) len += cmd.getCounts();

                if (len > maxLen) maxLen = len;
            }

            int sharedLen = 0;
            for (CommandPair cmd : shared) sharedLen += cmd.getCounts();

            diffCounts = (maxLen - sharedLen);

            if (diffCounts > 0) shared.add(new CommandPair(CommandPair.EMPTY, diffCounts));

            return shared;
        }
    }

    /**
     * Using the end position of the current move and the commands of all subsequent moves for a rank, updates the start and
     * end positions of each move based on the current move.  Useful for when the commands of a middle move are
     * changed and the later moves need to have their positions updated.
     * @param rankName - the rank whose positions need to be updated.
     */
    private void updatePositions(String rankName) {
        for (int i = currentMove+1; i < drillInfo.getMoves().size(); i++) {
            drillInfo.getMoves().get(i).updatePositions(rankName, drillInfo.getMoves().get(i-1).getEndPositions().get(rankName));
        }

        //TODO: may not be necessary later when you have to select ranks by clicking on them first
        selectedRanks.add(rankName);
        modified = true;
        mainView.updateSelectedRank(selectedRanks);
        mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
    }

    /**
     * Sets the selected rank to the specified rank and updates the view.
     *
     * @param rankName - the rank name of the new selected rank
     * @return - An error messsge iff setting the selected rank failed. An empty
     *         string otherwise.
     */
    @Override
    public String addSelectedRank(String rankName, boolean reset) {
        if (!drillInfo.doesRankExist(rankName)) {
            return "The selected rank does not exist";
        }

        if (reset) {
            selectedRanks.clear();
        }

        // TODO: decide what's more important: deselection from group, or ability to drag a group
        // if (selectedRanks.contains(rankName)) {
        //     selectedRanks.remove(rankName);
        // } else {
        selectedRanks.add(rankName);
        // }
        mainView.updateSelectedRank(selectedRanks);
        mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
        return "";
    }

    /**
     * Removes rankName selection, so that none are selected
     *
     */
    public void deselectAll() {
        selectedRanks.clear();
        mainView.clearSelectedRank();
        mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
    }

    /**
     * Returns the currently selected rank
     *
     * @return - the name of the selected rank
     */
    @Override
    public HashSet<String> getSelectedRanks() {
        return selectedRanks;
    }

    /**
     * Deletes the specified rank
     *
     * @param rankNames - the names of the ranks to be deleted
     */
    @Override
    public void deleteRank(HashSet<String> rankNames) {
        HashSet<String> oldSelectedRanks = (HashSet<String>)rankNames.clone();

        for(String rankName : oldSelectedRanks) {
            drillInfo.deleteRank(rankName);
            if (selectedRanks.contains(rankName)) {
                selectedRanks.remove(rankName);
            }
        }

        modified = true;
        mainView.updateSelectedRank(selectedRanks);
        mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
    }

    /**
     * Sets the song constants of this project: the tempo changes, count
     * changes, and name of the song
     *
     * @param tempoHashMap - a hashmap mapping the measure number of a tempo
     * change to the new tempo at this measure
     * @param countsHashMap - a hashmap mapping the measure number of a count
     * change to the new counts per measure
     * @param songName - the name of the song
     */
    @Override
    public void setSongConstants(HashMap<Integer, Integer> tempoHashMap, HashMap<Integer, Integer> countsHashMap, String songName) {
        drillInfo.setTempoHashMap(tempoHashMap);
        drillInfo.setCountsHashMap(countsHashMap);
        drillInfo.setSongName(songName);
        modified = true;

        // Must be called after setting modified = true so that the project title is updated to indicate the modification.
        mainView.updateProjectTitle();
    }

    /**
     * Plays back the animated project displayed to the user
     */
    @Override
    public void startPlayback() {
        //TODO: paint the present first!
        if (!isPlaybackRunning() && playbackMove < drillInfo.getMoves().size()) {
            playbackRunning = true;
            mainView.disableProjectButtons();
            playbackCount = 0;
            playbackMove = 1;
            playbackThread = new Thread(new PlaybackController(this));
            playbackThread.start();
            System.out.println("Playback Started.");
            mainView.updateFootballField(playbackMove, playbackCount);
        }
    }

    /**
     * Stops the playback and resumes the normal view for the user
     */
    public void mainThreadStopPlayback() {
        playbackRunning = false;
        playbackCount = 0;
        playbackCountTotal = 0;
        playbackMove = 1;
        mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
        if (playbackThread.isAlive()) {
            try {
                //playbackThread.interrupt();
                playbackThread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        playbackThread = null;
        mainView.enableProjectButtons();
        System.out.println("Playback Ended.");
    }

    /**
     * Increments the playback count showing the next step to the user
     */
    @Override
    public synchronized void incrementPlaybackCount() {
        //sanity check on the thread existing
        if (isPlaybackRunning()) {
            // TODO: consider making the other methods synchronized as well that way no method
            // from the controller may repaint the screen during playback
            // TODO: resizing the screen may fuck up playback....
            playbackCount++;
            playbackCountTotal++;
            if (playbackCount > drillInfo.getMoves().get(playbackMove).getCounts()) {//TODO: may be a concurrency issue here if someone else is editing the moves
                playbackCount = 1;
                playbackMove++;
            }
            if (playbackMove >= drillInfo.getMoves().size()) {//TODO: may be a concurrency issue here if someone else is editing the moves
                playbackRunning = false;
                playbackCount = 0;
                playbackCountTotal = 0;
                playbackMove = 1;
                mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
                playbackThread = null;
                mainView.enableProjectButtons();
                mainView.setPlaybackButtonState(playbackRunning);
                System.out.println("Playback Ended.");
            }
            else {
                mainView.updateFootballField(playbackMove, playbackCount);
                System.out.println("From the playbackThread - Move Number: " + playbackMove + ", Count: " + playbackCount);
            }

        }
    }

    private synchronized int getCountTotal() {
        return playbackCountTotal;
    }

    /**
     * Checks if playback is currently running
     *
     * @return - true if playback is running, false if not
     */
    @Override
    public synchronized boolean isPlaybackRunning() {
        return playbackRunning;
    }

    /**
     * Gets the rank positions from the playback thread to be displayed to the user
     *
     * @return - a hashmap mapping the rank name to its current position in playback
     */
    @Override
    public HashMap<String, RankPosition> getPlaybackPositions() {
        return drillInfo.getMoves().get(playbackMove).getIntermediatePositions(playbackCount);
    }

    /**
     * Gets the tempo changes throughout this song
     *
     * @return - a hashmap mapping the measure number of a tempo change to the new
     * tempo at this measure
     */
    @Override
    public HashMap<Integer, Integer> getTempoHash() {
        return drillInfo.getTempoHashMap();
    }

    /**
     * Gets the speed at which the playback is updated, based on the current count
     *
     * @return
     */
    @Override
    public synchronized int getPlaybackSpeed() {
        HashMap<Integer, Integer>countMap = getCountHash();
        HashMap<Integer, Integer>tempoMap = getTempoHash();
        if(tempoMap.isEmpty()) {
            return 500;
        }
        int meas;
        if(countMap.isEmpty()) {
            // default to 4/4 time
            int currentCount = getCountTotal();
            meas = (Math.max(currentCount-1,0))/4 + 1; // automatically truncates
        }
        else {
            Integer[] measCounts = new Integer[0];
            measCounts = countMap.keySet().toArray(measCounts);
            Arrays.sort(measCounts);

            int currentCount = getCountTotal();
            int prevM = 1;
            int prevC = 4;
            meas = 1;
            for(Integer measure : measCounts) {
                int countDiff = (measure - prevM)*prevC;
                if (currentCount - countDiff <= 0) {
                    // means the currentCount is in this interval
                    break;
                }
                else {
                    // means the currentCount is in the next interval
                    currentCount-=countDiff;
                    meas+=(measure-prevM);
                }
                prevM = (int)measure;
                prevC = countMap.get((int)measure);
            }
            // the last interval
            meas+=(Math.max(currentCount-1,0))/prevC;

        }
        Integer[] measTempos = new Integer[0];
        measTempos = tempoMap.keySet().toArray(measTempos);
        Arrays.sort(measTempos);

        int tempo = 120;
        for(Integer measure : measTempos) {
            if(meas < measure) {
                // that is, the measure we're working on is before the next change
                break;
            }
            else {
                // set the tempo
                tempo = tempoMap.get(measure);
            }
        }
        if(tempo<=0) {
            return 500;
        }
        return 60000/tempo;
    }

    /**
     * Gets the count changes throughout this song
     *
     * @return - a hashmap mapping the measure number of a count change to the new
     * counts per measure at this measure
     */
    @Override
    public HashMap<Integer, Integer> getCountHash() {
        return drillInfo.getCountsHashMap();
    }

    /**
     * Returns the song name of this project
     *
     * @return - the song name
     */
    @Override
    public String getSongName() {
        return drillInfo.getSongName();
    }

    /**
     * Assigns a command to the given rank in the current move. Returns an
     * error message if it could not add a command to the rank.
     *
     * @param rankName    - the rank to add a command to
     * @param commandPair - the command and number of counts
     * @return - An error messsge iff assigning the command failed. An empty string
     *         otherwise.
     */
    @Override
    public String assignCommand(String rankName, CommandPair commandPair){
        final String errorMessage = drillInfo.getMoves().get(currentMove).addCommand(rankName, commandPair);
        if (!errorMessage.isEmpty()) {
            return errorMessage;
        }
        updatePositions(rankName);
        return "";
    }

    /**
     * Removes the selected commands from the selected rank for the current move and
     * updates the display.
     *
     * @param commandIndices - the index of commands to be removed
     * @return - An error messsge iff removing the command failed. An empty string
     *         otherwise.
     */
    @Override
    public String removeCommands(int[] commandIndices) {
        HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
        ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);

        for (int indx : commandIndices) {
            if (sharedCommands.get(indx).getCommand() == CommandPair.EMPTY) {
                // If any of the requested commands are a mixed one, return an error and delete nothing
                return "Cannot delete mixed command";
            }
        }

        for (String rankName : selectedRanks) {
            int[] rankIndices = new int[commandIndices.length];

            for (int j = 0; j < commandIndices.length; j++) {
                int countsToIndex = 0;
                for (int i = 0; i < commandIndices[j]; i++) countsToIndex += sharedCommands.get(i).getCounts();

                for (int i = 0; i < allCommands.get(rankName).size(); i++) {
                    if (countsToIndex == 0) {
                        rankIndices[j] = i;
                        break;
                    } else if (countsToIndex < 0) {
                        return "Something went wrong! Could not remove commands.";
                    }
                    countsToIndex -= allCommands.get(rankName).get(i).getCounts();
                }
            }

            final String errorMessage = drillInfo.getMoves().get(currentMove).removeCommands(rankName, rankIndices);
            if (!errorMessage.isEmpty()) {
                return errorMessage;
            }
            updatePositions(rankName);
        }

        return "";
    }

    /**
     * Renames the specified command to a new name, but keep the same functionality.
     * Updates the display afterwards.
     *
     * @param index - the index of the command to be renamed
     * @param name  - the new name of the command
     * @return - An error messsge iff renaming the command failed. An empty string
     *         otherwise.
     */
    @Override
    public String renameCommand(int index, String name) {
        HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
        ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);

        if (sharedCommands.get(index).getCommand() == CommandPair.EMPTY) {
            return "Cannot rename mixed command";
        }

        for (String rankName : selectedRanks) {
            int rankIndex = -1;
            int countsToIndex = 0;
            for (int i = 0; i < index; i++) countsToIndex += sharedCommands.get(i).getCounts();

            for (int i = 0; i < allCommands.get(rankName).size(); i++) {
                if (countsToIndex == 0) {
                    rankIndex = i;
                    break;
                } else if (countsToIndex < 0) {
                    return "Something went wrong! Could not rename command.";
                }
                countsToIndex -= allCommands.get(rankName).get(i).getCounts();
            }

            final String errorMessage = drillInfo.getMoves().get(currentMove).renameCommand(rankName, rankIndex, name);
            if (!errorMessage.isEmpty()) {
                return errorMessage;
            }
            modified = true;
            mainView.updateSelectedRank(selectedRanks);
        }

        return "";
    }

    /**
     * Moves the specified commands up in the queue of commands. Updates the display
     * afterwards.
     *
     * @param commandIndices - the indices of commands to be moved up
     * @return - An error messsge iff moving the command failed. An empty string
     *         otherwise.
     */
    @Override
    public String moveCommandsUp(int[] commandIndices) {
        HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
        ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);

        for (int indx : commandIndices) {
            if (sharedCommands.get(indx).getCommand() == CommandPair.EMPTY) {
                return "Cannot move mixed command up";
            }
        }

        for (String rankName : selectedRanks) {
            int[] rankIndices = new int[commandIndices.length];

            for (int j = 0; j < commandIndices.length; j++) {
                int countsToIndex = 0;
                for (int i = 0; i < commandIndices[j]; i++) countsToIndex += sharedCommands.get(i).getCounts();

                for (int i = 0; i < allCommands.get(rankName).size(); i++) {
                    if (countsToIndex == 0) {
                        rankIndices[j] = i;
                        break;
                    } else if (countsToIndex < 0) {
                        return "Something went wrong! Could not move commands up.";
                    }
                    countsToIndex -= allCommands.get(rankName).get(i).getCounts();
                }
            }

            final String errorMessage = drillInfo.getMoves().get(currentMove).moveCommandsUp(rankName, rankIndices);
            if (!errorMessage.isEmpty()) {
                return errorMessage;
            }
            modified = true;
            mainView.updateSelectedRank(selectedRanks);
        }

        return "";
    }

    /**
     * Moves the specified commands down in the queue of commands. Updates the
     * display afterwards.
     *
     * @param commandIndices - the indices of commands to be moved down
     * @return - An error messsge iff moving the command failed. An empty string
     *         otherwise.
     */
    @Override
    public String moveCommandsDown(int[] commandIndices) {
        HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
        ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);

        for (int indx : commandIndices) {
            if (sharedCommands.get(indx).getCommand() == CommandPair.EMPTY) {
                return "Cannot move mixed command down";
            }
        }

        for (String rankName : selectedRanks) {
            int[] rankIndices = new int[commandIndices.length];

            for (int j = 0; j < commandIndices.length; j++) {
                int countsToIndex = 0;
                for (int i = 0; i < commandIndices[j]; i++) countsToIndex += sharedCommands.get(i).getCounts();

                for (int i = 0; i < allCommands.get(rankName).size(); i++) {
                    if (countsToIndex == 0) {
                        rankIndices[j] = i;
                        break;
                    } else if (countsToIndex < 0) {
                        return "Something went wrong! Could not move commands down.";
                    }
                    countsToIndex -= allCommands.get(rankName).get(i).getCounts();
                }
            }

            String errorMessage = drillInfo.getMoves().get(currentMove).moveCommandsDown(rankName, rankIndices);
            if (!errorMessage.isEmpty()) {
                return errorMessage;
                
            }
            modified = true;
            mainView.updateSelectedRank(selectedRanks);
        }

        return "";
    }

    /**
     * Merges the specified commands if they are of the same type
     * into one command of their combined length.  Updates the
     * display afterwards.
     *
     * @param commandIndices - the indices of commands to be merged
     * @return - An error messsge iff merging the command failed. An empty string
     *         otherwise.
     */
    @Override
    public String mergeCommands(int[] commandIndices) {
        HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
        ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);

        for (int indx : commandIndices) {
            if (sharedCommands.get(indx).getCommand() == CommandPair.EMPTY) {
                return "Cannot merge mixed commands";
            }

            if (sharedCommands.get(indx).getCommand() == CommandPair.DTP) {
                return "Cannot merge DTP commands";
            }
        }

        for (String rankName : selectedRanks) {
            int[] rankIndices = new int[commandIndices.length];

            for (int j = 0; j < commandIndices.length; j++) {
                int countsToIndex = 0;
                for (int i = 0; i < commandIndices[j]; i++) countsToIndex += sharedCommands.get(i).getCounts();

                for (int i = 0; i < allCommands.get(rankName).size(); i++) {
                    if (countsToIndex == 0) {
                        rankIndices[j] = i;
                        break;
                    } else if (countsToIndex < 0) {
                        return "Something went wrong! Could not merge commands.";
                    }
                    countsToIndex -= allCommands.get(rankName).get(i).getCounts();
                }
            }

            String errorMessage = drillInfo.getMoves().get(currentMove).mergeCommands(rankName, rankIndices);
            if (!errorMessage.isEmpty()) {
                return errorMessage;
            }
            modified = true;
            mainView.updateSelectedRank(selectedRanks);
        }

        return "";
    }

    /**
     * Returns true if the command at the given index can be split.  False otherwise.
     * 
     * @param index - The index of the command to split.
     * @return - True if the command at the given index can be split.  False otherwise.
     */
    @Override
    public String canSplit(int index) {
        final HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
        final ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);
        return canSplit(index, sharedCommands);
    }

    private String canSplit(int index, ArrayList<CommandPair> sharedCommands) {
        if (sharedCommands.get(index).getCommand() == CommandPair.EMPTY) {
            return "Cannot split mixed command";
        }
        if (sharedCommands.get(index).getCommand() == CommandPair.DTP) {
            return "Cannot split DTP command";
        }
        return "";
    }

    /**
     * Splits the specified command at the given index into two separate commands of
     * the same type at the count specified. Updates the display afterwards.
     *
     * @param index - the index of command to be split
     * @param count - the count at which the command will be split
     * @return - An error message if one occurs
     */
    @Override
    public String splitCommand(int index, int count) {
        HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
        ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);

        final String canSplitErrorMessage = canSplit(index, sharedCommands);
        if (!canSplitErrorMessage.isEmpty()) {
            return canSplitErrorMessage;
        }
        if (count >= sharedCommands.get(index).getCounts()) {
            return "The specified count was larger than counts in the move.";
        }

        for (String rankName : selectedRanks) {
            int rankIndex = -1;
            int countsToIndex = 0;
            for (int i = 0; i < index; i++) countsToIndex += sharedCommands.get(i).getCounts();

            for (int i = 0; i < allCommands.get(rankName).size(); i++) {
                if (countsToIndex == 0) {
                    rankIndex = i;
                    break;
                } else if (countsToIndex < 0) {
                    return "Something went wrong! Could not split the command.";
                }
                countsToIndex -= allCommands.get(rankName).get(i).getCounts();
            }

            final String errorMessage = drillInfo.getMoves().get(currentMove).splitCommand(rankName, rankIndex, count);
            if (!errorMessage.isEmpty()) {
                return errorMessage;
            }
            modified = true;
            mainView.updateSelectedRank(selectedRanks);
        }

        return "";
    }

    /**
     * Exports the project to a PDF file
     *
     * @param file - the save location of the new PDF file
     */
    @Override
    public void exportPDF(File file) {
        if (file == null) {
            mainView.displayError("No save location specified");
        }
        else {
            PDFGenerator pdfGenerator = new PDFGenerator();
            try {
                pdfGenerator.createPDF(drillInfo, file);
            } catch (IOException e) {
                mainView.displayError("IOException occurred in generating PDF");
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the comment for the current move
     *
     * @return - a string containing the comment for this move
     */
    @Override
    public String getMoveComment() {
        return drillInfo.getMoves().get(currentMove).getComments();
    }

    /**
     * Sets the comment for the current move
     *
     * @param comment - a string containing the comment for this move
     */
    @Override
    public void setMoveComment(String comment) {
        drillInfo.getMoves().get(currentMove).setComments(comment);
        modified = true;

        // Must be called after setting modified = true so that the project title is updated to indicate the modification.
        mainView.updateProjectTitle();
    }

    /**
     * Returns the number of the current move
     *
     * @return - the number of the current move
     */
    @Override
    public int getCurrentMoveNumber() {
        return currentMove;
    }

    /**
     * Returns the current move.
     * 
     * @return - the current move.
     */
    public Move getCurrentMove() {
        return drillInfo.getMoves().get(currentMove);
    }

}


