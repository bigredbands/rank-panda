package org.bigredbands.mb.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import org.bigredbands.mb.controllers.ControllerInterface;
import org.bigredbands.mb.models.Field;

public class MoveScrollBar {

    private ControllerInterface controller;

    private JScrollPane moveScrollPane;
    private JPanel moveContainer;

    private final int THUMBNAIL_WIDTH = 131; //Sweet spot that no horizontal scroll bar is needed
    private final Dimension MOVE_THUMBNAIL_SIZE = new Dimension(THUMBNAIL_WIDTH, THUMBNAIL_WIDTH);

    private final Dimension MOVE_THUMBNAIL_MARGIN = new Dimension(0,20);

    private final Dimension MOVE_LABEL_MARGIN = new Dimension(0,5);

    private final Dimension MOVE_LABEL_SIZE = new Dimension(10, 20);

    private ArrayList<MoveThumbnail> thumbnailList = new ArrayList<MoveThumbnail>();

    private ArrayList<JPanel> moveComponentList = new ArrayList<JPanel>();

    public MoveScrollBar(ControllerInterface controller) {
        this.controller = controller;

        //create the jscrollpane
        moveScrollPane = new JScrollPane();
        moveScrollPane.setBorder(new LineBorder(Color.darkGray,1,true));
        moveScrollPane.setPreferredSize(new Dimension(150,900));
        moveScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    //TODO: this function may only be useful when initializing, so possibly combine them
    public void createNewScrollBar(int numberOfMoves) {
        //create a new jpanel to add things too
        moveContainer = new JPanel();
        moveContainer.setLayout(new BoxLayout(moveContainer,BoxLayout.Y_AXIS));

        for (int i = 0; i < numberOfMoves; i++) {
            addMove(i);
        }

        //This panel is necessary to allow the buttons to resize
        //moveContainer.add(spaceFillingPanel);

        //set the panel to be the view in the view port
        moveScrollPane.setViewportView(moveContainer);

        //set to visible
        moveContainer.setVisible(true);
        moveScrollPane.setVisible(true);
    }

    public void addMoveToScrollBar(int moveNumber) {
        //remove the space filling panel
        //moveContainer.remove(spaceFillingPanel);

        //add the move
        addMove(moveNumber);

        //add the space filling panel.  this panel is necessary to allow the buttons to resize
        //moveContainer.add(spaceFillingPanel);

        //set to visible
        moveContainer.setVisible(true);
        moveScrollPane.setVisible(true);
    }

    public void removeMoveFromScrollBar(int moveNumber) {
        //remove the space filling panel
        //moveContainer.remove(spaceFillingPanel);

        //add the move
        deleteMove(moveNumber);

        //add the space filling panel.  this panel is necessary to allow the buttons to resize
        //moveContainer.add(spaceFillingPanel);

        //set to visible
        moveContainer.setVisible(true);
        moveScrollPane.setVisible(true);
    }

    public void repaintScrollBar() {
        for (MoveThumbnail move : thumbnailList) {
            move.repaint();
        }
    }

    private void addMove(final int moveNumber) {
        JPanel oneMoveContainer = new JPanel();
        oneMoveContainer.setLayout(new BoxLayout(oneMoveContainer, BoxLayout.Y_AXIS));
        oneMoveContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        MoveThumbnail moveThumbnail = new MoveThumbnail(Field.CollegeFootball, controller, moveNumber);
        moveThumbnail.setPreferredSize(MOVE_THUMBNAIL_SIZE);
        moveThumbnail.setAlignmentX(Component.CENTER_ALIGNMENT);
        oneMoveContainer.add(moveThumbnail);

        oneMoveContainer.add(Box.createRigidArea(MOVE_LABEL_MARGIN));

        JLabel moveLabel = new JLabel("Move " + moveNumber);
        moveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        oneMoveContainer.add(moveLabel);

        // Space between moveThumbnails, this is necessary so they don't press
        // up against each other
        oneMoveContainer.add(Box.createRigidArea(MOVE_THUMBNAIL_MARGIN));

        moveContainer.add(oneMoveContainer);
        if(moveComponentList.size() > moveNumber) {
            moveComponentList.set(moveNumber, oneMoveContainer);
            thumbnailList.set(moveNumber, moveThumbnail);
        } else {
            moveComponentList.add(moveNumber, oneMoveContainer);
            thumbnailList.add(moveNumber, moveThumbnail);
        }
    }

    private void deleteMove(final int moveNumber) {
        // remove an existing move
        // TODO: fill in the function (make sure to include sanity checks!)
        // TODO: also, ask for confirmation of deletion (because implementing "undo" would suck)
        // TODO: ^ tru fax

        // Note: this is hacky as shit, but it works. I guess.
        int numMoves = thumbnailList.size()-1;

        for(int i = moveNumber; i<numMoves+1; i++) {
            moveContainer.remove(moveComponentList.get(moveNumber));
            thumbnailList.remove(moveNumber);
            //moveComponentList.get(moveNumber).setVisible(false);
            moveComponentList.remove(moveNumber);
            moveContainer.repaint();
        }
        for(int i = moveNumber; i<numMoves; i++) {
            addMove(i);
        }


    }

    public JScrollPane getScrollPane() {
        return moveScrollPane;
    }
}
