package org.bigredbands.mb.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;

import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.RankPosition;


/**
 * This is the interface the mainView will implement with all necessary
 * functions needed by the controller. 
 */
public interface ViewInterface {
    
    /**
     * Update the scrollbar by completely recreating the moves in the scrollbar 
     * with the new numberOfMoves.  Then update the screen by repainting everything
     * and changing the move to moveNumber
     * 
     * @param numberOfMoves - the number of moves to display in the scrollbar
     * @param moveNumber - the move number to display on the screen
     */
    public void updateViewWithMoves(int numberOfMoves, int moveNumber, int countNumber);

    /**
     * Display an error dialog to the user telling them useful information for
     * what went wrong.
     * 
     * @param errorMessage
     */
    public void displayError(String errorMessage);
    
    /**
     * Add a new move the the scrollbar with the given moveNumber.  Then update
     * the screen by repainting everything and changing the move to moveNumber.
     * 
     * @param moveNumber - the current move to display on the screen
     */
    public void updateViewWithOneMove(int moveNumber, int countNumber);
    
    /**
     * Update the screen by repainting the field, repainting the scrollbar, 
     * and updating the displayed move number
     * 
     * @param moveNumber - the current move number to display to the user
     */
    public void updateView(int moveNumber, int countNumber);
    
    /**
     * Updates only the football field.  Useful for playback.
     * 
     * @param moveNumber - the current move number to be displayed
     */
    public void updateFootballField(int moveNumber, int countNumber);
    
    /**
     * Creates the intro screen and displays it to the user
     */
    public void createIntroView();
    
    /**
     * Creates the project screen and displays it to the user
     */
    public void createProjectView();
    
    /**
     * Checks if the project screen has already been created
     * 
     * @return - true if the project screen has been created, false if not
     */
    public boolean isProjectViewCreated();
    
    /**
     * Updates the selected rank text on the screen and the currently displayed commands.
     * 
     * @param rankNames - the rank names to display
     * @param commands - the commands to display in the command list.
     */
    void updateSelectedRank(HashSet<String> rankNames,
            ArrayList<CommandPair> commands);
    
    /**
     * Disables the listeners of the buttons in project view for playback
     */
    public void disableProjectButtons();
    
    /**
     * Enables the listeners of the buttons in project view after playback
     */
    public void enableProjectButtons();
    
    /**
     * Change the playback button based on if playback is currently running.
     * 
     * @param isPlaybackRunning - true if playback is running, false if not
     */
    public void setPlaybackButtonState(boolean isPlaybackRunning);

    void updateViewWithRemoveMove(int moveNumber, int countNumber,
            int removeMove);

    public HashMap<String,RankPosition> getRankPositions();

    
}
