package org.bigredbands.mb.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.models.RankPosition;

/**
 *
 * This is the interface the mainController will implement
 *
 */
public interface ControllerInterface {

    /**
     * Saves the project at its current location
     */
    public void saveProject();

    /**
     * Saves the project at the location fileUrl
     *
     * @param file - the location to save the file at
     */
    public void saveProject(File file);

    /**
     * Loads the project at the location fileUrl
     *
     * @param file - the file to be loaded
     * @return true if the file is successfully loaded, false if an error occurs
     */
    public boolean loadProject(File file);

    /**
     * Creates a new blank project
     */
    public void createEmptyProject();

    /**
     * Checks if this project has been previously saved
     *
     * @return true if this project has not been saved before, false if it has
     */
    public boolean isInitialSave();

    /**
     * Exits the program cleanly
     */
    public void closeProgram();

    /**
     * Returns true if the underlying drill data has been modified after loading or creating new drill.
     * 
     * @return true if the drill has been modified, false otherwise.
     */
    public boolean isModified();

    /**
     * Adds a new rank to the models and update the view
     *
     * @param name - the name of the new rank
     * @param rankPosition - the position of the new rank
     */
    public void addRank(String name, RankPosition rankPosition);

    /**
     * Adds a new move to the list of moves
     *
     * TODO: may change the return type later
     * @param counts - the number of counts in the move
     */
    public void addMove(int counts);

    /**
     * Returns the number of moves in the project
     *
     * @return - the number of moves
     */
    public int getNumberOfMoves();

    /**
     * Returns each of the rank's name and corresponding position at the current
     * move
     *
     * @return - a hashmap mapping each rank name to its position
     */
    public HashMap<String, RankPosition> getRankPositions();

    /**
     * Returns each of the rank's name and corresponding position at the
     * specified move number
     *
     * @param moveNumber
     *            - the move number of which to get the rank names and positions
     * @return - a hashmap mapping each rank name to its position
     */
    public HashMap<String, RankPosition> getRankPositions(int moveNumber);

    /**
     * Switches the displayed move to the specified move
     *
     * @param targetMove - the new move which to display in the view
     */
    public void changeMoves(int targetMove);

    /**
     * Assigns a command to the given rank in the current move. Displays an
     * error message if it could not add a command to the rank.
     *
     * @param rankName
     *            - the rank to add a command to
     * @param commandPair
     *            - the command and number of counts
     */
    public void assignCommand(String rankName, CommandPair commandPair);

    /**
     * Sets the selected rank to the specified rank
     *
     * @param rankName - the rank name of the new selected rank
     */
    public void addSelectedRank(String rankName, boolean reset);

    /**
     * Removes any current rank selection
     *
     */
    public void deselectAll();

    /**
     * Returns the currently selected rank
     *
     * @return - the name of the selected rank
     */
    public HashSet<String> getSelectedRanks();

    /**
     * Deletes the specified rank
     *
     * @param rankName - the name of the rank to be deleted
     */
    void deleteRank(HashSet<String> rankNames);

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
    public void setSongConstants(HashMap<Integer, Integer> tempoHashMap,
            HashMap<Integer, Integer> countsHashMap, String songName);

    /**
     * Removes the selected commands from the selected rank for the current move
     *
     * @param commandIndices - the index of commands to be removed
     */
    public void removeCommands(int[] commandIndices);

    /**
     * Gets the tempo changes throughout this song
     *
     * @return - a hashmap mapping the measure number of a tempo change to the new
     * tempo at this measure
     */
    public HashMap<Integer, Integer> getTempoHash();

    /**
     * Gets the count changes throughout this song
     *
     * @return - a hashmap mapping the measure number of a count change to the new
     * counts per measure at this measure
     */
    public HashMap<Integer, Integer> getCountHash();

    /**
     * Returns the song name of this project
     *
     * @return - the song name
     */
    public String getSongName();

    /**
     * Plays back the animated project displayed to the user
     */
    public void startPlayback();

    /**
     * Stops the playback and resumes the normal view for the user
     */
    public void mainThreadStopPlayback();

    /**
     * Checks if playback is currently running
     *
     * @return - true if playback is running, false if not
     */
    public boolean isPlaybackRunning();

    /**
     * Gets the rank positions from the playback thread to be displayed to the user
     *
     * @return - a hashmap mapping the rank name to its current position in playback
     */
    public HashMap<String, RankPosition> getPlaybackPositions();

    /**
     * Rename the specified command to a new name, but keep the same functionality
     *
     * @param index - the index of the command to be renamed
     * @param name - the new name of the command
     */
    public void renameCommand(int index, String name);

    /**
     * Moves the specified commands up in the queue of commands
     *
     * @param commandIndices - the indices of commands to be moved up
     */
    public void moveCommandsUp(int[] commandIndices);

    /**
     * Moves the specified commands down in the queue of commands
     *
     * @param commandIndices - the indices of commands to be moved down
     */
    public void moveCommandsDown(int[] commandIndices);

    /**
     * Merges the specified commands if they are of the same type
     * into one command of their combined length
     *
     * @param commandIndices - the indices of commands to be merged
     */
    public void mergeCommands(int[] commandIndices);

    /**
     * Splits the specified command into two separate commands of the same type
     * at the count specified
     *
     * @param index - the index of command to be split
     * @param count - the count at which the command will be split
     * @return - An error message if one occurs
     */
    public String splitCommand(int index, int count);

    /**
     * Exports the project to a PDF file
     *
     * @param file - the save location of the new PDF file
     */
    public void exportPDF(File file);

    /**
     * Returns the comment for the current move
     *
     * @return - a string containing the comment for this move
     */
    public String getMoveComment();

    /**
     * Sets the comment for the current move
     *
     * @param comment - a string containing the comment for this move
     */
    public void setMoveComment(String comment);

    /**
     * Returns the number of the current move.
     *
     * @return - the number of the current move.
     */
    public int getCurrentMoveNumber();

    /**
     * Returns the current move.
     * 
     * @return - the current move.
     */
    public Move getCurrentMove();

    public void updateInitialPosition(String rankName, RankPosition newPos);

    public void deleteMove(int moveNum);

    /**
     * Gets the set of shared commands for the given rankNames and their commands.
     * 
     * @param rankNames - The rankNames for which to fetch shared commands.
     * @param commands - The commands for which to fetch shared commands.
     * @return An ArrayList containing the shared commands.
     */
    public ArrayList<CommandPair> getSharedCommands(HashSet<String> rankNames, HashMap<String, ArrayList<CommandPair>> commands);

}
