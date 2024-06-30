package org.bigredbands.mb.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.bigredbands.mb.controllers.MainController;

/**
 * This class creates the initial window after the program is called.
 *
 * It creates an initial window with three buttons, centered both horizontally and vertically
 *
 * Button 1: New - creates a new project
 *
 * Button 2: Load - loads an existing project
 *
 * Button 3: Cancel - exits the program
 */
public class IntroView {

    //window which everything will be displayed on
    private JFrame window;
    //size of the buttons
    private static final Dimension BUTTON_SIZE = new Dimension(100,20);
    //used to interact with the controller
    private MainView mainView;

    public IntroView(MainView main) {
        this.mainView = main;

        //Set up the defining characteristics of the window
        window = new JFrame("RankPanda 2.0");
        window.setSize(400, 300);
        window.setMinimumSize(new Dimension(200, 200));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);

        //The top level container in which all other things will be placed
        JPanel container = new JPanel();
        window.add(container);

        //Use GridBagLayout to organize the buttons
        GridBagConstraints grid = new GridBagConstraints();
        container.setLayout(new GridBagLayout());
        JPanel newButtonPanel = new JPanel();
        grid.gridx = 1;
        grid.gridy = 0;
        container.add(newButtonPanel, grid);

        //Set up New Button, which when clicked will launch a new project.
        JButton newButton = new JButton("New");
        newButton.setPreferredSize(BUTTON_SIZE);
        newButton.setToolTipText("Open a new blank project");
        newButtonPanel.add(newButton, BorderLayout.CENTER);
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                exit();
                mainView.createNewProject();
            }
        });

        JPanel loadButtonPanel = new JPanel();
        grid.gridx = 1;
        grid.gridy = 1;
        container.add(loadButtonPanel, grid);

        //Set up Load Button, which when clicked will load an existing project
        JButton loadButton = new JButton("Load");
        loadButton.setPreferredSize(BUTTON_SIZE);
        loadButton.setToolTipText("Load an existing project");
        loadButtonPanel.add(loadButton, BorderLayout.CENTER);
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                mainView.loadProject();
            }
        });

        JPanel cancelButtonPanel = new JPanel();
        grid.gridx = 1;
        grid.gridy = 2;
        container.add(cancelButtonPanel, grid);

        //Set up Cancel Button, which when clicked will close the program
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(BUTTON_SIZE);
        cancelButton.setToolTipText("Cancel and close the program");
        cancelButtonPanel.add(cancelButton, BorderLayout.CENTER);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                exit();
                mainView.closeProgram();
            }
        });
    }

    public void Draw() {
        window.setVisible(true);
    }

    //Return the window of this IntroView
    public JFrame getWindow() {
        return window;
    }

    //Exit the intro view closing the intro window
    public void exit() {
        window.dispose();
    }
}
