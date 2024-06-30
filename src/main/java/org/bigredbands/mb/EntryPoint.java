package org.bigredbands.mb;

import org.bigredbands.mb.controllers.MainController;

/**
 *
 * This class handles launching the program and creates the controller
 *
 */
public class EntryPoint {

    /**
     * @param args - String args passed from the command line. Currently unused.
     */
    public static void main(String[] args) {
        final MainController.ControllerViewBundle controllerViewBundle = MainController.BuildMainControllerAndView();

        // Creates and draws the initial view of the program.
        controllerViewBundle.getView().createIntroView();

    }

}
