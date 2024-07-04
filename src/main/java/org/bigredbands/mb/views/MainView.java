package org.bigredbands.mb.views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.bigredbands.mb.controllers.ControllerInterface;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.Move;
import org.bigredbands.mb.models.RankPosition;
import org.bigredbands.mb.exceptions.FileSelectionException;


/**
 * This class handles which screen to display and handles calling the controller.
 * It also handles similar calls made by different windows, such as saving and loading
 * both from the intro view and the project view.
 */
public class MainView implements ViewInterface {

    // The class for the intro window
    private IntroView intro;

    // The class for the project window
    private ProjectView project;

    // The controller to pass information to
    private ControllerInterface controller;

    // The FileChooser used for saving and loading project files
    private JFileChooser fileChooser;

    // The name of the loaded file
    private String projectTitle = "New Project";

    // Whether the next rank should be snapped to the grid
    private boolean exactGrid = false;

    private int pointSelected = 0;

    private boolean needToDrag=false;

    /**
     * The constructor that prepares this class for use
     *
     * @param controller - the controller for the project to send user inputs to
     */
    public MainView(ControllerInterface controller) {
        //store a handle to the controller
        this.controller = controller;

        //initialize the MainView Class
        initialize();
    }

    /**
     * Create the intro view and display it
     */
    public void createIntroView() {
        intro = new IntroView(this);
        // Now that everything on the window is set up, display the window
        intro.Draw();

    }

    /**
     * This method sets up everything that needs to be initialized
     */
    private void initialize() {
        //Intialize the file chooser used to open and save files
        initializeFileChooser();
    }

    /**
     * Create the project view and display it
     * 
     * @param showWizard - whether the Song Constants wizard should be shown when creating the project view
     */
    public void createProjectView(boolean showWizard) {
        project = new ProjectView(this, controller, showWizard);
    }

    /**
     * Checks if a project view has been created yet
     * True if project view created, false if not created
     */
    public boolean isProjectViewCreated() {
        return !(project == null);
    }


    /**
     * Update the scrollbar by completely recreating the moves in the scrollbar
     * with the new numberOfMoves.  Then update the screen by repainting everything
     * and changing the move to moveNumber
     *
     * @param numberOfMoves - the number of moves to display in the scrollbar
     * @param moveNumber - the move number to display on the screen
     */
    @Override
    public void updateViewWithMoves(int numberOfMoves, int moveNumber, int countNumber) {
        updateView(moveNumber, countNumber);
        project.createNewScrollBar(numberOfMoves);
    }

    /**
     * Add a new move the the scrollbar with the given moveNumber.  Then update
     * the screen by repainting everything and changing the move to moveNumber.
     *
     * @param moveNumber - the current move to display on the screen
     */
    @Override
    public void updateViewWithOneMove(int moveNumber, int countNumber) {
        updateView(moveNumber, countNumber);
        project.addMoveToScrollBar(moveNumber);
    }

    /**
     * Add a new move the the scrollbar with the given moveNumber.  Then update
     * the screen by repainting everything and changing the move to moveNumber.
     *
     * @param moveNumber - the current move to display on the screen
     */
    @Override
    public void updateViewWithRemoveMove(int moveNumber, int countNumber, int removeMove) {
        updateView(moveNumber, countNumber);
        project.removeMoveFromScrollBar(removeMove);
    }

    /**
     * Update the screen by repainting the field, repainting the scrollbar,
     * and updating the displayed move number
     *
     * @param moveNumber - the current move number to display to the user
     */
    @Override
    public void updateView(int moveNumber, int countNumber) {
        updateFootballField(moveNumber, countNumber);
        updateProjectTitle();
        project.repaintScrollBar();
    }

    /**
     * Updates only the football field.  Useful for playback.
     *
     * @param moveNumber - the current move number to be displayed
     */
    @Override
    public void updateFootballField(int moveNumber, int countNumber) {
        project.displayMoveNumber(moveNumber, countNumber);
        project.repaintFieldPanel();
    }

    /**
     * Returns the number of moves in the project, used by MoveScrollBar
     *
     * @return - the number of moves in the project
     */
    public int getNumberOfMoves() {
        return controller.getNumberOfMoves();
    }

    public RankPosition getDTPRank() {
        return project.getDTPDest();
    }

    public RankPosition getFTARank() {
        return project.getFTADest();
    }

    public void updateInitialPosition(String rankName, RankPosition newPos) {
        controller.updateInitialPosition(rankName, newPos);
    }

    /**
     * Updates the selected rank text on the screen and the currently displayed commands.
     *
     * @param rankName - the rank name to display
     * @param commands - the commands to display in the command list.
     */
    @Override
    public void updateSelectedRank(HashSet<String>rankNames, ArrayList<CommandPair> commands) {
        project.updateSelectedRank(rankNames);
        project.updateCommandList(commands);
        updateProjectTitle();
    }

    /**
     * Called by the listeners of command buttons in ProjectView
     * Check validity of rank name and count
     * If valid, assign the command using the controller
     *
     * @param rankName - the rank name to assign a command to
     * @param countString - the string representation of the number of counts
     * @param type - the type of command
     */
    public void assignCommand(HashSet<String>rankNames, String countString, int type, RankPosition dest) {
        //Check for empty rank name
        if (rankNames.isEmpty()) {
            displayError("Please specify rank name(s)");
            return;
        }

        //Convert the count to an integer
        int count = 0;
        try {
            count = Integer.parseInt(countString);
        }
        //Display an error if the count is not a number
        catch (NumberFormatException e){
            displayError("Please enter number into count");
            return;
        }
        //Display an error if the count is not positive
        if (count <= 0) {
            displayError("Please enter positive number into count");
            return;
        }
        //The name and count are both valid, so assign the command
        CommandPair newCommand = new CommandPair(type, count);

        if (type == CommandPair.DTP && rankNames.size() > 1) {
            displayError("Cannot create DTP for multiple ranks");
            return;
        }

        // TODO: add functionality for FTA
        if (type == CommandPair.DTP) {
            if(dest == null) {
                displayError("Please set destination");
                return;
            }
            newCommand.setDestination(dest);
        }

        for(String rankName : rankNames) {
            controller.assignCommand(rankName,  newCommand);
        }
        project.cancelPreviousCommand(type);
        return;
    }

    /**
     * Display an error dialog to the current window
     *
     * @param errorMessage - the error message to display
     */
    public void displayError(String errorMessage) {
        new ErrorDialog(errorMessage, getCurrentWindow());
    }

    /**
     * Creates an empty project, can be called from introView and projectView
     * The controller method will respond appropriately
     */
    public void createNewProject () {
        controller.createEmptyProject();
        projectTitle = "New Project";
        project.updateProjectTitle();
    }

    /**
     * Loads a project, can be called from introView and projectView
     * The controller method will respond appropriately
     */
    public void loadProject() {
        boolean realLoad = controller.loadProject(getOpenLocation());
        if (realLoad == true) {
            project.updateProjectTitle();
            intro.exit();
        }
    }

    /**
     * Closes the program, and exits all windows
     */
    public void closeProgram() {
        controller.closeProgram();
    }

    /**
     * Returns the current primary window, used for error dialogs
     * @return
     */
    public JFrame getCurrentWindow() {
        if (isProjectViewCreated()) {
            return project.getWindow();
        }
        else {
            return intro.getWindow();
        }
    }

    /**
     * Sets the currently selected rank
     * @param rankName - the currently selected rank
     */
    public void addSelectedRank(String rankName, boolean reset) {
        controller.addSelectedRank(rankName,reset);
    }

    /**
     * Sets the currently selected rank
     * @param rankName - the currently selected rank
     */
    public void deselectAll() {
        controller.deselectAll();
    }

    public boolean getCtrlPress() {
        return project.getCtrlPress();
    }

    /**
     * Returns the HashMap of rankNames to rankPositions at the current move
     * @return the positions of the ranks for the current move
     */
    public HashMap<String,RankPosition> getRankPositions() {
        return controller.getRankPositions();
    }

    /**
     * Returns the HashMap of rankNames to rankPositions at moveNumber
     * @param moveNumber - the move number of the rank positions we want
     * @return the rank positions of the ranks for the specified move
     */
    public HashMap<String,RankPosition> getRankPositions(int moveNumber) {
        return controller.getRankPositions(moveNumber);
    }

    /**
     * Returns the fileName, used by ProjectView to properly title the window
     * @return the file name of the project
     */
    public String getProjectTitle() {
        return projectTitle;
    }

    /**
     * Update the title of the window to the current fileName.
     */
    @Override
    public void updateProjectTitle() {
        project.updateProjectTitle();
    }

    /**
     * Gets the currently selected rank, if one exists.
     * Returns the empty string if no rank is selected.
     * @return the currently selected rank or the empty string if no rank is currently selected.
     */
    public HashSet<String> getSelectedRanks() {
        return controller.getSelectedRanks();
    }

    /**
     * Deletes the specified rank from the project
     * @param rankName - the name of the rank to be deleted
     */
    public void deleteRank(HashSet<String>rankNames) {
        controller.deleteRank(rankNames);
    }

    /**
     * Called by listener in ProjectView
     * Check validity of moveLength
     * If valid tell the controller to add the move
     * @param moveLengthString
     */
    public void addMove(String moveLengthString) {
        //Convert the moveLength to an integer
        int moveLength;
        try {
            moveLength = Integer.parseInt(moveLengthString);
        }
        //Display an error if moveLength is not a number
        catch (NumberFormatException e) {
            displayError("Please enter a number into move length");
            return;
        }
        //Display an error if moveLength is negative
        if (moveLength <= 0) {
            displayError("Please enter a positive number into move length");
            return;
        }
        //moveLength is valid; tell the controller to add the move
        controller.addMove(moveLength);
    }

    /**
     * Called by listener in ProjectView
     * Check validity of moveLength
     * If valid tell the controller to add the move
     * @param moveLengthString
     */
    public void deleteMove(int moveNum) {
        //Convert the moveLength to an integer
        //Display an error if moveNum is negative
        if (moveNum <= 0 || moveNum >= getNumberOfMoves()) {
            return;
        }
        //moveNum is valid; tell the controller to remove the move
        controller.deleteMove(moveNum);
    }

    /**
     * Switch the current displayed move
     * @param moveNumber - the move to change to
     */
    public void changeMoves(int moveNumber) {
        controller.changeMoves(moveNumber);
    }

    /**
     * Add a rank to the project
     * @param name - name of the rank
     * @param position - the position of the rank
     */
    public void addRank(String name, RankPosition position) {
        controller.addRank(name, position);
    }

    /**
     * Remove the selected commands for the given rank
     *
     * @param commandIndices - the indices of the commands to remove
     */
    public void removeCommands(int[] commandIndices) {
        controller.removeCommands(commandIndices);
    }

    /**
     * Rename the specified command to a new name, but keep the same functionality
     *
     * @param index - the index of the command to be renamed
     * @param name - the new name of the command
     */
    public void renameCommand(int index, String name) {
        controller.renameCommand(index, name);
    }

    /**
     * Moves the specified commands up in the queue of commands
     *
     * @param commandIndices - the indices of commands to be moved up
     */
    public void moveCommandsUp(int[] commandIndices) {
        controller.moveCommandsUp(commandIndices);
    }

    /**
     * Moves the specified commands down in the queue of commands
     *
     * @param commandIndices - the indices of commands to be moved down
     */
    public void moveCommandsDown(int[] commandIndices) {
        controller.moveCommandsDown(commandIndices);
    }

    /**
     * Merges the specified commands if they are of the same type
     * into one command of their combined length
     *
     * @param commandIndices - the indices of commands to be merged
     */
    public void mergeCommands(int[] commandIndices) {
        controller.mergeCommands(commandIndices);
    }

    /**
     * Splits the specified command into two separate commands of the same type
     * at the count specified
     *
     * @param index - the index of command to be split
     * @param count - the count at which the command will be split
     * @return - An error message if one occurs
     */
    public String splitCommand(int index, int count) {
        return controller.splitCommand(index, count);
    }

    /**
     * Starts playback if it is not currently running.
     * Stops playback if it is running.
     */
    public void togglePlayback() {
        //change the state
        if (controller.isPlaybackRunning()) {
            controller.mainThreadStopPlayback();
        }
        else {
            controller.startPlayback();
        }

        //display new state
        setPlaybackButtonState(controller.isPlaybackRunning());
    }

    /**
     * Change the playback button based on if playback is currently running.
     *
     * @param isPlaybackRunning - true if playback is running, false if not
     */
    public void setPlaybackButtonState(boolean isPlaybackRunning) {
        project.setPlaybackButtonState(isPlaybackRunning);
    }

    /**
     * Checks if playback is currently running
     *
     * @return - true if playback is running, false if not
     */
    public boolean isPlaybackRunning() {
        return controller.isPlaybackRunning();
    }

    /**
     * Gets the rank positions from the playback thread to be displayed to the user
     *
     * @return - a hashmap mapping the rank name to its current position in playback
     */
    public HashMap<String, RankPosition> getPlaybackPositions() {
        return controller.getPlaybackPositions();
    }

    /**
     * Disables the listeners of the buttons in project view for playback
     */
    public void disableProjectButtons() {
        if (project != null) {
            project.disableProjectButtons();
        }
    }

    /**
     * Enables the listeners of the buttons in project view after playback
     */
    public void enableProjectButtons() {
        if (project != null) {
            project.enableProjectButtons();
        }
    }

    /**
     * Returns the comment for the current move
     *
     * @return - a string containing the comment for this move
     */
    public String getMoveComment() {
        return controller.getMoveComment();
    }

    /**
     * Sets the comment for the current move
     *
     * @param comment - a string containing the comment for this move
     */
    public void setMoveComment(String comment) {
        controller.setMoveComment(comment);
    }

    /**
     * Returns the number of the current move
     *
     * @return - the number of the current move
     */
    public int getCurrentMoveNumber() {
        return controller.getCurrentMoveNumber();
    }

    /**
     * Returns the current move.
     * 
     * @return - the current move.
     */
    public Move getCurrentMove() {
        return controller.getCurrentMove();
    }

    /**
     * Checks if the next rank drawn should be snapped to the grid
     *
     * @return - True if the rank should be snapped to the grid, false if not
     */
    public boolean isExactGrid() {
        return exactGrid;
    }

    /**
     * Sets whether the next rank drawn should be snapped to the grid
     *
     * @param exactGrid - True if the rank should be snapped to the grid, false if not
     */
    public void setExactGrid(boolean exactGrid) {
        this.exactGrid = exactGrid;
    }

    /**
     * Saves the project at its current location. If no location is specified,
     * prompt the user for the save location
     * 
     * @return - True if a file was chosen, false if the save process was canceled
     */
    public boolean saveProject() throws FileSelectionException {
        if (controller.isInitialSave()) {
            if (getSaveLocation() == null) {
                return false;
            }
            controller.saveProject(getSaveLocation());
        }
        else {
            controller.saveProject();
            project.updateProjectTitle();
        }
        return true;
    }

    /**
     * Save the project and have the user specify a save location
     * 
     * @return - True if a file was chosen, false if the save process was canceled
     */
    public boolean saveProjectAs() throws FileSelectionException{
        controller.saveProject(getSaveLocation());
        project.updateProjectTitle();
        return true;
    }

    /**
     * Export the project to a pdf and have the user specify a save location
     */
    public void exportPDF() {
        JFileChooser pdfFileChooser = new JFileChooser();
        pdfFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        pdfFileChooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("pdf files", "pdf");
        pdfFileChooser.setFileFilter(filter);
        pdfFileChooser.addChoosableFileFilter(filter);

        int returnValue = pdfFileChooser.showSaveDialog(getCurrentWindow());
        File file;

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String path = pdfFileChooser.getSelectedFile().getPath();
            path = path.lastIndexOf(".pdf") > 0 ? path.substring(0, path.lastIndexOf(".pdf")) : path;
            file = new File (path + ".pdf");
        }
        else {
            file = null;
        }
        controller.exportPDF(file);
    }

    /**
     * Prepare the fileChooser to be used
     */
    private void initializeFileChooser() {
        //Create a file chooser
        fileChooser = new JFileChooser();

        //only allow the user to only select one file
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        //only allows the user to access .pnd files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("pnd files","pnd");
        fileChooser.setFileFilter(filter);
        fileChooser.addChoosableFileFilter(filter);
    }

    /**
     * Gets the file that the user would like to open by prompting them with a JFileChooser
     *
     * @return - the file the user selected to open
     */
    private File getOpenLocation() {
        //In response to a button click in the dialog
        int returnVal = fileChooser.showOpenDialog(getCurrentWindow());

        //if the user chose something, return it.  if not, return null.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            //TODO: we should probably log this at some point.....
            System.out.println(file.getAbsolutePath());
            projectTitle = file.getName();
            return file;
        }
        else {
            return null;
        }
    }

    /**
     * Gets the file that the user would like to save by prompting them with a JFileChooser
     *
     * @return - the file the user would like to save, null if the save process was canceled
     */
    private File getSaveLocation() throws FileSelectionException {
        //In response to a button click in the dialog
        int returnVal = fileChooser.showSaveDialog(getCurrentWindow());

        //if the user chose something, return it.  if not, return null.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getPath();
            path = path.lastIndexOf(".pnd") > 0 ? path.substring(0, path.lastIndexOf(".pnd")) : path;
            File file = new File(path + ".pnd");

            //TODO: we should probably log this at some point.....
            System.out.println(file.getAbsolutePath());
            projectTitle = file.getName();
            return file;
        } else if (returnVal == JFileChooser.CANCEL_OPTION) {
            return null;
        } else { // JFileChooser.ERROR_OPTION
            throw new FileSelectionException("Failed to select file.");
        }
    }

    /**
     * Sets the selected point to the specified value
     * @param pointSelected - the new selected point
     */
    public void setSelectPoint(int pointSelected) {
        this.pointSelected=pointSelected;
    }

    /**
     * Gets the currently selected point
     * @return - the currently selected point
     */
    public int getSelectPoint(){
        return pointSelected;
    }

    /**
     * Set whether the ranks can be dragged
     * @param b - true if the ranks can be dragged, false if not
     */
    public void setDrag(boolean b) {
        needToDrag=b;
    }

    /**
     * Gets whether the ranks can be dragged
     * @return - true if the ranks can be dragged, false if not
     */
    public boolean getDrag(){
        return needToDrag;
    }
}
