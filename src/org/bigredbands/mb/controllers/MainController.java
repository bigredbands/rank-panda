package org.bigredbands.mb.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.exceptions.COSVisitorException;
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
	
	// Additional visual indicators to be displayed to the user, but not stored as ranks
	private HashMap<String, RankPosition> transientRanks;
	
	// The name of the indicator used to show the user how their rank will look if drawn
	// at the mouse's current position
	public static final String TEMP_DRAWING_RANK = "TEMP_DRAWING_RANK";
	
	// The number of the current move
	private int currentMove;
	
	// The name of the selected rank
	private HashSet<String> selectedRanks = new HashSet<String>();
	
	// The main thread handling all normal processes
	private Thread mainThread;
	
	// The playback thread used exclusively when displaying playback to the user
	private Thread playbackThread = null;
	
	// True if playback is currently running, false if not
	private boolean playbackRunning = false;
	
	// The current count of the playback animation
	private int playbackCount = 0;
	
	// The current move of the playback animation
	private int playbackMove = 1;
	
	private int playbackCountTotal = 0;
	
	/**
	 * The constructor that prepares this class for use
	 */
	public MainController() {
		initialize();
	}
	
	/**
	 * Initializes this class for use
	 */
	private void initialize() {
		//TODO: used to initialize with createnewproject, but cant do that any more due to view code in the function
		//Initialize all necessary variables
		fileUrl = "";
		drillInfo = new DrillInfo();
		transientRanks = new HashMap<String, RankPosition>();
		currentMove = 0;
		selectedRanks.clear();
		
		//TODO: fill me in! initialize the view and anything else that needs to be done here
		//Set up the main view and display the intro screen to the user
		mainView = new MainView(this);
		mainView.createIntroView();
		
		mainThread = Thread.currentThread();
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
			mainView.createProjectView();
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
			mainView.createProjectView();
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
	 * @param moveNumber
	 *            - the move number of which to get the rank names and positions
	 * @return - a hashmap mapping each rank name to its position
	 */
	@Override
	public HashMap<String, RankPosition> getRankPositions(int moveNumber) {
		//TODO for Dave: Sanity check moveNumber to see if it actually exists
		//TODO: thanks Dave! -Victoria
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
			mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
		}
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
			mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
		}
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
				mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks, drillInfo.getMoves().get(currentMove).getCommands()));
			}
			mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
		}
	}
	
	/**
	 * Adds a new rank to the models and update the view
	 * 
	 * @param name - the name of the new rank
	 * @param rankPosition - the position of the new rank
	 */
	@Override
	public void addRank(String rankName, RankPosition rankPosition) {
		String errorMessage = drillInfo.addRankToMoves(rankName, rankPosition);
		if (errorMessage.isEmpty()) {
			// TODO: maybe not clear? idk
			selectedRanks.clear();
			selectedRanks.add(rankName);
			mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
			mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
		}
		else {
			mainView.displayError(errorMessage);
		}
	}
	
	public void updateInitialPosition(String rankName, RankPosition newPos) {
		drillInfo.getMoves().get(0).updatePositions(rankName,newPos);
		updatePositions(rankName);
		
	}
	
	private ArrayList<CommandPair> getSharedCommands(HashSet<String> rankNames, HashMap<String,ArrayList<CommandPair>>commands) {
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
									if (diffCounts != 0) shared.add(new CommandPair(32, diffCounts));
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
			
			if (diffCounts > 0) shared.add(new CommandPair(32, diffCounts));
			
			return shared;
		}
	}
	
	/**
	 * Using the end position of the current move and the commands of all subsequent moves for a rank, updates the start and
	 * end positions of each move based on the current move.  Useful for when the commands of an middle move are
	 * changed and the later moves need to have their positions updated.
	 * @param rankName - the rank whose positions need to be updated.
	 */
	private void updatePositions(String rankName) {
		for (int i = currentMove+1; i < drillInfo.getMoves().size(); i++) {
			drillInfo.getMoves().get(i).updatePositions(rankName, drillInfo.getMoves().get(i-1).getEndPositions().get(rankName));
		}
		
		//TODO: may not be necessary later when you have to select ranks by clicking on them first
		selectedRanks.add(rankName); 
		mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
		mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
	}

	/**
	 * Sets the selected rank to the specified rank
	 * 
	 * @param rankName - the rank name of the new selected rank
	 */
	@Override
	public void addSelectedRank(String rankName, boolean reset) {
		if (drillInfo.doesRankExist(rankName)) {
			if(reset) selectedRanks.clear();
				
			// TODO: decide what's more important: deselection from group, or ability to drag a group
//			if (selectedRanks.contains(rankName)) {
//				selectedRanks.remove(rankName);
//			} else {
			selectedRanks.add(rankName);
//			}
			mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
			mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
		}
		else {
			mainView.displayError("The selected rank does not exist");
		}
	}
	
	/**
	 * Removes rankName selection, so that none are selected
	 * 
	 */
	public void deselectAll() {
		selectedRanks.clear();
		mainView.updateSelectedRank(new HashSet<String>(), new ArrayList<CommandPair>());
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
	 * @param rankName - the name of the rank to be deleted
	 */
	@Override
	public void deleteRank(HashSet<String>rankNames) {
		HashSet<String> oldSelectedRanks = (HashSet<String>)rankNames.clone();
		
		for(String rankName : oldSelectedRanks) {
			drillInfo.deleteRank(rankName);
			if (selectedRanks.contains(rankName)) {
				selectedRanks.remove(rankName);
				mainView.updateSelectedRank(new HashSet<String>(), new ArrayList<CommandPair>());
			}
			else {
				mainView.updateSelectedRank(selectedRanks, drillInfo.getMoves().get(currentMove).getCommands().get(rankName));
			}
		}
		
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
	 * Assigns a command to the given rank in the current move. Displays an
	 * error message if it could not add a command to the rank.
	 * 
	 * @param rankName
	 *            - the rank to add a command to
	 * @param commandPair
	 *            - the command and number of counts
	 */
	@Override
	public void assignCommand(String rankName, CommandPair commandPair){
		String errorMessage = drillInfo.getMoves().get(currentMove).addCommand(rankName, commandPair);
		if (errorMessage.isEmpty()) {
			updatePositions(rankName);
		}
		else {
			mainView.displayError(errorMessage);
		}
	}
	
	/**
	 * Removes the selected commands from the selected rank for the current move
	 * 
	 * @param commandIndices - the index of commands to be removed (in the SHARED list)
	 */
	@Override
	public void removeCommands(int[] commandIndices) {
		HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
		ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);
		
		for (int indx : commandIndices) {
			if (sharedCommands.get(indx).getCommand() == 32) {
				// If any of the requested commands are a mixed one, return an error and delete nothing
				mainView.displayError("Cannot delete mixed command");
				return;
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
						mainView.displayError("Something went wrong! Could not remove commands.");
						return;
					}
					countsToIndex -= allCommands.get(rankName).get(i).getCounts();
				}
			}
			
			String errorMessage = drillInfo.getMoves().get(currentMove).removeCommands(rankName, rankIndices);
			if (errorMessage.isEmpty()) {
				updatePositions(rankName);
			}
			else {
				mainView.displayError(errorMessage);
			}
		}
	}

	/**
	 * Rename the specified command to a new name, but keep the same functionality
	 * 
	 * @param index - the index of the command to be renamed
	 * @param name - the new name of the command
	 */
	@Override
	public void renameCommand(int index, String name) {
		HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
		ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);
		
		if (sharedCommands.get(index).getCommand() == 32) {
			mainView.displayError("Cannot rename mixed command");
			return;
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
					mainView.displayError("Something went wrong! Could not rename command.");
					return;
				}
				countsToIndex -= allCommands.get(rankName).get(i).getCounts();
			}
			
			String errorMessage = drillInfo.getMoves().get(currentMove).renameCommand(rankName, rankIndex, name);
			if (errorMessage.isEmpty()) {
				mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
			}
			else {
				mainView.displayError(errorMessage);
			}
		}
		
	}

	/**
	 * Moves the specified commands up in the queue of commands
	 * 
	 * @param commandIndices - the indices of commands to be moved up
	 */
	@Override
	public void moveCommandsUp(int[] commandIndices) {
		HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
		ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);
		
		for (int indx : commandIndices) {
			if (sharedCommands.get(indx).getCommand() == 32) {
				mainView.displayError("Cannot move mixed command up");
				return;
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
						mainView.displayError("Something went wrong! Could not move commands up.");
						return;
					}
					countsToIndex -= allCommands.get(rankName).get(i).getCounts();
				}
			}
			
			String errorMessage = drillInfo.getMoves().get(currentMove).moveCommandsUp(rankName, rankIndices);
			if (errorMessage.isEmpty()) {
				mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
			}
			else {
				mainView.displayError(errorMessage);
			}
		}
		
	}

	/**
	 * Moves the specified commands down in the queue of commands
	 * 
	 * @param commandIndices - the indices of commands to be moved down
	 */
	@Override
	public void moveCommandsDown(int[] commandIndices) {
		HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
		ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);
		
		for (int indx : commandIndices) {
			if (sharedCommands.get(indx).getCommand() == 32) {
				mainView.displayError("Cannot move mixed command down");
				return;
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
						mainView.displayError("Something went wrong! Could not move commands down.");
						return;
					}
					countsToIndex -= allCommands.get(rankName).get(i).getCounts();
				}
			}
			
			String errorMessage = drillInfo.getMoves().get(currentMove).moveCommandsDown(rankName, rankIndices);
			if (errorMessage.isEmpty()) {
				mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
			}
			else {
				mainView.displayError(errorMessage);
			}		
		}
	}

	/** 
	 * Merges the specified commands if they are of the same type
	 * into one command of their combined length
	 * 
	 * @param commandIndices - the indices of commands to be merged
	 */
	@Override
	public void mergeCommands(int[] commandIndices) {
		HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
		ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);
		
		for (int indx : commandIndices) {
			if (sharedCommands.get(indx).getCommand() == 32) {
				mainView.displayError("Cannot merge mixed commands");
				return;
			}
			
			if (sharedCommands.get(indx).getCommand() == 19) {
				mainView.displayError("Cannot merge DTP commands");
				return;
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
						mainView.displayError("Something went wrong! Could not merge commands.");
						return;
					}
					countsToIndex -= allCommands.get(rankName).get(i).getCounts();
				}
			}
			
			String errorMessage = drillInfo.getMoves().get(currentMove).mergeCommands(rankName, rankIndices);
			if (errorMessage.isEmpty()) {
				mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
			}
			else {
				mainView.displayError(errorMessage);
			}
		}
	}

	/**
	 * Splits the specified command into two separate commands of the same type
	 * at the count specified
	 * 
	 * @param index - the index of command to be split
	 * @param count - the count at which the command will be split
	 * @return - An error message if one occurs
	 */
	@Override
	public String splitCommand(int index, int count) {
		// TODO: this check should really be included int he splitCommand function, not here, but I 
		// dont want it to close
		// the window if this check is done, but i do want it to close it for others...  
		// this will need to be re coded

		HashMap<String,ArrayList<CommandPair>> allCommands = drillInfo.getMoves().get(currentMove).getCommands();
		ArrayList<CommandPair> sharedCommands = getSharedCommands(selectedRanks, allCommands);
		
		if (count >= sharedCommands.get(index).getCounts()) {
			return "The specified count was larger than counts in the move.";
		}
		
		if (sharedCommands.get(index).getCommand() == 32) {
			mainView.displayError("Cannot split mixed command");
			return "";
		}	
		
		if (sharedCommands.get(index).getCommand() == 19) {
			mainView.displayError("Cannot split DTP command");
			return "";
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
					mainView.displayError("Something went wrong! Could not rename rank.");
					return "";
				}
				countsToIndex -= allCommands.get(rankName).get(i).getCounts();
			}
			
			String errorMessage = drillInfo.getMoves().get(currentMove).splitCommand(rankName, rankIndex, count);
			if (errorMessage.isEmpty()) {
				mainView.updateSelectedRank(selectedRanks, getSharedCommands(selectedRanks,drillInfo.getMoves().get(currentMove).getCommands()));
			}
			else {
				mainView.displayError(errorMessage);
			}
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
			} catch (COSVisitorException e) {
				// TODO Auto-generated catch block
				mainView.displayError("COSVisitorException occurred in generating PDF");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
	}
	
	/**
	 * Returns the number of the current move
	 * 
	 * @return - the number of the current move
	 */
	@Override
	public int getCurrentMove() {
		return currentMove;
	}
	
	/**
	 * Returns additional indicators to be displayed to the user not stored as ranks
	 * 
	 * @return - a hashmap mapping the name of the indicator to its position
	 */
	@Override
	public HashMap<String, RankPosition> getTransientRanks() {
		return transientRanks;
	}

	/**
	 * Adds a temporary indicator to show the user how the rank would look if drawn to
	 * the current position
	 * 
	 * @param temporaryDrawingRank - the position of the indicator to be drawn
	 */
	@Override
	public void addTemporaryDrawingRank(RankPosition temporaryDrawingRank) {
		transientRanks.put(TEMP_DRAWING_RANK, temporaryDrawingRank);
		mainView.updateView(currentMove, drillInfo.getMoves().get(currentMove).getCounts());
	}
}


